package com.healthbridge.exceptionhandler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.healthbridge.dto.ApiResult;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final String DESC = "Description";

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		Map<String, String> errors = new HashMap<>();

		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String message = error.getDefaultMessage();
			errors.put(fieldName, message);
		});

		ApiResult<Map<String, String>> response = ApiResult.fail("Validation failed", errors);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	// Handle user not found
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiResult<?>> handleUserNotFound(UserNotFoundException ex) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(DESC, "User lookup failed");
		return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(ApiResult.fail(ex.getMessage()));
	}

	// Handle illegal arguments
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResult<?>> handleIllegalArgument(IllegalArgumentException ex) {
		return ResponseEntity.badRequest().body(ApiResult.fail(ex.getMessage()));
	}

	// Catch-all for unexpected exceptions
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResult<?>> handleGeneralException(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResult.fail("Unexpected error: " + ex.getMessage()));
	}
}
