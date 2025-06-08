// src/app/services/app-user.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, catchError, map, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from '../auth.service';

// Define the AddressDTO interface
export interface AddressDTO {
  countryRegion?: string;
  pincode?: string;
  flatHouseNoBuildingCompanyApartment?: string;
  areaStreetSectorVillage?: string;
  landmark?: string;
  townCity?: string;
  state?: string;
}

export interface AppUserDTO {
  userId?: number;
  name: string;
  email: string;
  dob?: string; // ISO Date string
  phone: string;
  password?: string; // Optional: Only send if updating password
  isActive?: boolean;
  role?: string;
  address?: AddressDTO; // Added AddressDTO
}

export interface RegistrationRequest {
  name: string;
  email: string;
  password: string;
  otp: string;
  phone: string;
  dob?: string;
  role?: string;
  address?: AddressDTO; // Added AddressDTO
}

export interface OtpRequest {
  email: string;
}

export interface OtpVerificationResponse {
  verified: boolean;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class AppUserService {
  private baseUrl = `${environment.apiUrl}/api/users`;
  private authUrl = `${environment.apiUrl}/api/auth`;

  constructor(private http: HttpClient, private authService: AuthService) { }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  registerUser(user: RegistrationRequest): Observable<string> {
    return this.http.post(
      `${this.baseUrl}/register`,
      user,
      { responseType: 'text' }
    ).pipe(
      catchError(this.handleError)
    );
  }

  sendRegistrationOtp(email: string): Observable<string> {
    return this.http.post(
      `${this.authUrl}/send-registration-otp`,
      { email },
      { responseType: 'text' }
    ).pipe(
      catchError(this.handleError)
    );
  }

  verifyOtp(email: string, otp: string): Observable<OtpVerificationResponse> {
    return this.http.post(
      `${this.authUrl}/verify-otp`,
      { email, otp },
      { responseType: 'text' }
    ).pipe(
      map((response: string) => {
        const verified = response.includes("successfully") || response.includes("verified");
        return {
          verified: verified,
          message: response
        };
      }),
      catchError(this.handleError)
    );
  }

  getUserById(userId: number): Observable<AppUserDTO> {
    return this.http.get<AppUserDTO>(`${this.baseUrl}/${userId}`, { headers: this.getAuthHeaders() })
      .pipe(
        catchError(this.handleError)
      );
  }

  getAllUsers(): Observable<AppUserDTO[]> {
    return this.http.get<AppUserDTO[]>(this.baseUrl, { headers: this.getAuthHeaders() })
      .pipe(
        catchError(this.handleError)
      );
  }

  updateUser(userId: number, user: AppUserDTO): Observable<AppUserDTO> {
    const payload: Partial<AppUserDTO> = {
      name: user.name,
      email: user.email,
      phone: user.phone,
      dob: user.dob,
      isActive: user.isActive,
      role: user.role,
      address: user.address // Pass the address object
    };

    if (user.password && user.password !== '') {
      payload.password = user.password;
    }

    return this.http.put<AppUserDTO>(
      `${this.baseUrl}/${userId}`,
      payload,
      { headers: this.getAuthHeaders() }
    ).pipe(
      catchError(this.handleError)
    );
  }

  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${userId}`, { headers: this.getAuthHeaders() })
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      if (error.error && typeof error.error === 'string') {
        errorMessage = error.error;
      } else if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else if (error.message) {
        errorMessage = error.message;
      } else {
        errorMessage = `Server returned code: ${error.status}, error message: ${error.message}`;
      }
    }
    console.error('AppUserService Error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}