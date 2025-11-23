import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule,RouterModule],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';
  isPasswordVisible = false;

  constructor(private authService: AuthService, private router: Router) {}

  togglePasswordVisibility() {
    this.isPasswordVisible = !this.isPasswordVisible;
  }

  onLogin(form: NgForm) {
  if (form.invalid) {
    form.control.markAllAsTouched();
    return;
  }

  this.authService.login(this.username, this.password).subscribe({
  next: () => {
    const role = this.authService.getUserRole();
    const lang = localStorage.getItem("userLanguage");
    if (lang) {
    setTimeout(() => {
      (window as any).switchLang(lang);
    }, 1200);
  }
    if (role === 'ROLE_ADMIN' || role === 'ADMIN') {
      this.router.navigate(['/admin']);
    } else {
      this.router.navigate(['/dashboard']);
    }
  },
  error: () => (this.error = 'Invalid username or password'),
});

}


}
