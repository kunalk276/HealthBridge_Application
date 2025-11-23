import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<any> {
    const credentials = btoa(`${username}:${password}`);
    
    const headers = new HttpHeaders({
      'Authorization': `Basic ${credentials}`,
      'Content-Type': 'application/json'
    });

    return this.http.get(`${this.apiUrl}/me`, { headers }).pipe(
      tap((response: any) => {
        localStorage.setItem('auth', credentials);
        
        const userData = response.data || response;
        localStorage.setItem('user', JSON.stringify(userData));
        
        if (userData.id) {
          localStorage.setItem('userId', userData.id.toString());
        }
        if (userData.username) {
          localStorage.setItem('username', userData.username);
        }
        if (userData.phone) {
          localStorage.setItem('userPhone', userData.phone);
        }
        if (userData.role) {
          localStorage.setItem('userRole', userData.role);
        }
         if (userData.language) {
    localStorage.setItem('userLanguage', userData.language);
  }
  localStorage.setItem('password', password);
      })
    );
  }

  logout() {
    localStorage.removeItem('auth');
    localStorage.removeItem('user');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('userPhone');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userLanguage');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('auth');
  }

  getAuthHeader(): string | null {
    const auth = localStorage.getItem('auth');
    return auth ? `Basic ${auth}` : null;
  }

  getCurrentUser(): any {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  getUserId(): number | null {
    const userId = localStorage.getItem('userId');
    return userId ? parseInt(userId, 10) : null;
  }

  getLoggedInUser(): string | null {
    return localStorage.getItem('username');
  }

  getUserPhone(): string | null {
    return localStorage.getItem('userPhone');
  }

  getUserRole(): string | null {
    return localStorage.getItem('userRole');
  }

  getUserLanguage(): string | null {
  return localStorage.getItem('userLanguage');
}



}