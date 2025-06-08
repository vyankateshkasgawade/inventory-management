// src/app/auth.service.ts

import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt';
import { BehaviorSubject, throwError, Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface JwtAuthResponse {
  token: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private authUrl = `${environment.apiUrl}/api/auth`;
  loggedInUser = new BehaviorSubject<JwtAuthResponse | null>(null);

  constructor(
    private http: HttpClient,
    private router: Router,
    private jwtHelper: JwtHelperService
  ) {
    this.loadTokenFromStorage();
  }

  private loadTokenFromStorage() {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      if (token) {
        this.loggedInUser.next({ token });
        console.log('AuthService: Token loaded from storage.');
      }
    }
  }

  login(formData: { username: string; password: string }): Observable<JwtAuthResponse> {
    return this.http.post<JwtAuthResponse>(`${this.authUrl}/login`, formData).pipe(
      tap((res: JwtAuthResponse) => {
        this.saveToLocalStorage(res);
        this.loggedInUser.next(res);

        const payload: any = this.jwtHelper.decodeToken(res.token);
        const userId = Number(payload?.userId);
        const roles = Array.isArray(payload?.roles) ? payload.roles : (payload?.roles ? [payload.roles] : []);

        console.log('AuthService: Decoded JWT Payload:', payload);
        console.log('AuthService: Extracted roles from payload:', roles);

        if (!isNaN(userId)) {
          localStorage.setItem('userId', userId.toString());
        }

        localStorage.setItem('roles', JSON.stringify(roles));

        this.router.navigate(['/dashboard']);
      }),
      catchError((err) => {
        console.error('Login failed:', err);
        this.loggedInUser.next(null);
        return throwError(() => err);
      })
    );
  }

  /**
   * Registers a new user with the provided data.
   * @param userData The user registration data (e.g., username, email, password).
   * @returns An Observable that emits when the registration is successful or throws an error.
   */
  register(userData: any): Observable<any> {
    return this.http.post(`${this.authUrl}/register`, userData).pipe(
      tap(() => {
        console.log('AuthService: Registration successful.');
        this.router.navigate(['/login']); // Redirect to login after successful registration
      }),
      catchError(error => {
        console.error('Registration failed:', error);
        // Throw a custom error with a more user-friendly message
        return throwError(() => new Error(error.error?.message || 'Registration failed. Please try again.'));
      })
    );
  }

  /**
   * Initiates the "forgot password" process by sending a reset email.
   * @param emailData An object containing the user's email address.
   * @returns An Observable that emits when the request is successful or throws an error.
   */
  forgotPassword(emailData: { email: string }): Observable<string> { // <-- Change return type to string
    return this.http.post(`${this.authUrl}/forgot-password`, emailData, { responseType: 'text' }).pipe( // <-- ADD responseType: 'text' here
      tap((response: string) => { // <-- Ensure tap expects a string
        console.log('AuthService: Forgot password request sent successfully. Response:', response);
      }),
      catchError(error => {
        console.error('Forgot password request failed:', error);
        // Ensure you're extracting the message correctly for plain text errors
        // If error.error is the plain text, use it directly.
        // If it's a HttpErrorResponse, use its error property.
        const errorMessage = typeof error.error === 'string' ? error.error : (error.message || 'Could not send reset email. Please try again.');
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  // --- START OF NEWLY ADDED METHODS ---

  verifyOtp(otpData: { email: string, otp: string }): Observable<string> {
    return this.http.post(`${this.authUrl}/verify-otp`, otpData, { responseType: 'text' }).pipe(
      tap((response: string) => {
        console.log('AuthService: OTP verification successful. Response:', response);
      }),
      catchError(error => {
        console.error('AuthService: OTP verification failed:', error);
        const errorMessage = typeof error.error === 'string' ? error.error : (error.message || 'Failed to verify OTP. Please try again.');
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  resetPassword(resetData: { email: string, otp: string, newPassword: string }): Observable<string> {
    return this.http.post(`${this.authUrl}/reset-password`, resetData, { responseType: 'text' }).pipe(
      tap((response: string) => {
        console.log('AuthService: Password reset successful. Response:', response);
      }),
      catchError(error => {
        console.error('AuthService: Password reset failed:', error);
        const errorMessage = typeof error.error === 'string' ? error.error : (error.message || 'Failed to reset password. Please try again.');
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  // --- END OF NEWLY ADDED METHODS ---


  logout(): void {
    const token = this.getToken();
    if (token) {
      const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
      this.http.post(`${this.authUrl}/logout`, null, { headers }).subscribe({
        next: () => console.log('Logged out successfully from backend'),
        error: (err) => console.error('Backend logout failed:', err),
      });
    }

    if (typeof window !== 'undefined') {
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      localStorage.removeItem('roles');
      console.log('AuthService: Cleared local storage and logged out from frontend.');
    }

    this.loggedInUser.next(null);
    this.router.navigate(['/login']);
  }

  private saveToLocalStorage(data: JwtAuthResponse): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem('token', data.token);
      console.log('AuthService: Token saved to localStorage.');
    }
  }

  getToken(): string | null {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      return token;
    }
    return null;
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    const authenticated = token != null && !this.jwtHelper.isTokenExpired(token);
    return authenticated;
  }

  isLoggedIn(): boolean {
    return this.isAuthenticated();
  }

  redirectIfLoggedIn(): void {
    if (this.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
      console.log('AuthService: Redirecting to dashboard (already logged in).');
    }
  }

  getCurrentUserId(): number | null {
    const token = this.getToken();
    if (!token) {
      console.warn('AuthService: No token found. Cannot get current user ID.');
      return null;
    }

    try {
      const payload: any = this.jwtHelper.decodeToken(token);
      const userId = Number(payload?.userId || payload?.sub);

      if (!isNaN(userId) && userId > 0) {
        return userId;
      } else {
        console.error('AuthService: Invalid or missing userId in token payload:', payload);
        return null;
      }
    } catch (e) {
      console.error('AuthService: Error decoding token to get current user ID:', e);
      return null;
    }
  }

  getUserIdFromToken(): number | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload: any = this.jwtHelper.decodeToken(token);
      const userId = Number(payload?.userId);
      return isNaN(userId) ? null : userId;
    } catch (e) {
      console.error('AuthService: Error decoding token for userId:', e);
      return null;
    }
  }

  getRoles(): string[] {
    if (typeof window !== 'undefined') {
      const rolesString = localStorage.getItem('roles');
      if (rolesString) {
        try {
          const parsedRoles = JSON.parse(rolesString);
          const rolesArray = Array.isArray(parsedRoles) ? parsedRoles : [parsedRoles];
          console.log('AuthService: getRoles() retrieved and parsed roles:', rolesArray);
          return rolesArray;
        } catch (e) {
          console.error('AuthService: Error parsing roles from localStorage:', e);
          return [];
        }
      }
    }
    console.log('AuthService: getRoles() - No roles found in localStorage.');
    return [];
  }

  hasRole(role: string): boolean {
    const userRoles = this.getRoles();
    const hasSpecificRole = userRoles.includes(role);
    console.log(`AuthService: hasRole("${role}") check. User has roles:`, userRoles, `Result: ${hasSpecificRole}`);
    return hasSpecificRole;
  }

  hasAnyRole(roles: string[]): boolean {
    const userRoles = this.getRoles();
    const hasAny = roles.some(role => userRoles.includes(role));
    console.log(`AuthService: hasAnyRole(${JSON.stringify(roles)}) check. User has roles:`, userRoles, `Result: ${hasAny}`);
    return hasAny;
  }
}