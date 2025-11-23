package com.healthbridge.config;


import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.healthbridge.service.impl.UserDetailsServiceImpl;

/**
 * Centralized security configuration.
 * - Public endpoints: /api/auth/register, /api/sms/**
 * - USER or ADMIN: /api/symptoms/**, /api/analytics/user/**
 * - ADMIN only: /api/admin/**, /api/analytics/**
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private UserDetailsServiceImpl userDetailsServiceImpl;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// Build AuthenticationManager using custom UserDetailsService
	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
		builder.userDetailsService(userDetailsServiceImpl).passwordEncoder(passwordEncoder());
		return builder.build();
	}

	// Main security filter chain
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable()) // APIs are stateless
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests(auth -> auth
						// allow preflight CORS
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

						// public endpoints
						.requestMatchers("/api/auth/register").permitAll()
						.requestMatchers("/api/sms/**").permitAll()

						// admin
						.requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
						.requestMatchers("/api/analytics/user/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

						// user (and admin) endpoints
						.requestMatchers("/api/symptoms/**", "/api/analytics/user/**")
						.hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

						// authenticated endpoints
						.requestMatchers("/api/auth/me").authenticated()

						// everything else requires authentication
						.anyRequest().authenticated()
				)
				// use basic auth (for simple backend-to-backend / debugging); switch to token filter if needed
				.httpBasic(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}

	// CORS configuration for Angular dev server
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:4200"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
