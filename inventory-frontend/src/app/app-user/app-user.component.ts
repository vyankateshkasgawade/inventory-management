// src/app/app-user/app-user.component.ts

import { Component, OnInit } from '@angular/core';
// Import AddressDTO
import { AppUserDTO, AppUserService, RegistrationRequest, AddressDTO } from '../services/app-user.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

// Angular Material modules
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-app-user',
  standalone: true,
  templateUrl: './app-user.component.html',
  styleUrls: ['./app-user.component.css'],
  imports: [
    CommonModule,
    FormsModule,

    // Angular Material modules used in this component
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatIconModule,
    MatTooltipModule,
  ],
})
export class AppUserComponent implements OnInit {
  users: AppUserDTO[] = [];
  selectedUser?: AppUserDTO; // This can be undefined
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  showRegistrationForm = false;

  registrationData: RegistrationRequest = {
    name: '',
    email: '',
    password: '',
    otp: '',
    phone: '',
    dob: '',
    role: 'USER',
    address: { // Always initialize address for new registrations
      countryRegion: '',
      pincode: '',
      flatHouseNoBuildingCompanyApartment: '',
      areaStreetSectorVillage: '',
      landmark: '',
      townCity: '',
      state: ''
    }
  };

  otpSent = false;
  otpVerified = false;

  constructor(
    private userService: AppUserService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadUsers();
  }

  toggleRegistrationForm() {
    this.showRegistrationForm = !this.showRegistrationForm;
    if (!this.showRegistrationForm) {
      this.resetForm();
    }
  }

  loadUsers() {
    this.isLoading = true;
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load users: ' + err.message;
        this.isLoading = false;
      }
    });
  }

  sendOtp() {
    if (!this.registrationData.email) {
      this.errorMessage = 'Email is required';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.userService.sendRegistrationOtp(this.registrationData.email).subscribe({
      next: (response: string) => {
        this.otpSent = true;
        this.successMessage = response;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to send OTP';
        this.isLoading = false;
      }
    });
  }

  verifyOtp() {
    if (!this.registrationData.otp) {
      this.errorMessage = 'OTP is required';
      return;
    }

    this.isLoading = true;
    this.userService.verifyOtp(this.registrationData.email, this.registrationData.otp).subscribe({
      next: (response) => {
        this.otpVerified = response.verified;
        this.successMessage = response.message;
        this.isLoading = false;

        if (!response.verified) {
          this.errorMessage = "Invalid OTP code";
        }
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.isLoading = false;
      }
    });
  }

  registerUser() {
    if (!this.otpVerified) {
      this.errorMessage = 'Please verify OTP first';
      return;
    }

    this.isLoading = true;
    this.userService.registerUser(this.registrationData).subscribe({
      next: (response: string) => {
        this.successMessage = response;
        this.resetForm();
        this.loadUsers();
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.isLoading = false;
      }
    });
  }

  resetForm() {
    this.registrationData = {
      name: '',
      email: '',
      password: '',
      otp: '',
      phone: '',
      dob: '',
      role: 'USER',
      address: { // Reset address when resetting form
        countryRegion: '',
        pincode: '',
        flatHouseNoBuildingCompanyApartment: '',
        areaStreetSectorVillage: '',
        landmark: '',
        townCity: '',
        state: ''
      }
    };
    this.otpSent = false;
    this.otpVerified = false;
    this.errorMessage = '';
    this.successMessage = '';
    this.showRegistrationForm = false;
  }

  selectUser(user: AppUserDTO) {
    // Deep copy the user object to avoid modifying the original list data
    this.selectedUser = JSON.parse(JSON.stringify(user));

    // IMPORTANT: Use a type guard or non-null assertion here before accessing properties.
    // Since we just assigned `this.selectedUser`, we can now confidently assert it's not undefined.
    // The previous checks might not have been enough to satisfy the compiler on the *next* line.

    if (this.selectedUser) { // Type guard to ensure selectedUser is not undefined
      // Handle date formatting if dob exists
      if (this.selectedUser.dob) {
        this.selectedUser.dob = this.formatDateForInput(this.selectedUser.dob);
      }

      // Ensure address object is initialized for editing, even if null from backend
      if (!this.selectedUser.address) {
        this.selectedUser.address = {
          countryRegion: '',
          pincode: '',
          flatHouseNoBuildingCompanyApartment: '',
          areaStreetSectorVillage: '',
          landmark: '',
          townCity: '',
          state: ''
        };
      }
    }
    this.showRegistrationForm = false; // Hide registration form when selecting to edit
  }

  private formatDateForInput(dateString: string): string {
    return dateString.split('T')[0]; // ISO format handling
  }

  updateUser() {
    // Type guard: Ensure selectedUser and selectedUser.userId are not undefined
    if (!this.selectedUser || !this.selectedUser.userId) {
      this.errorMessage = 'No user selected for update or user ID is missing.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    // Create a payload, ensuring address is included.
    // TypeScript knows selectedUser is not undefined here due to the `if` guard.
    const payload: AppUserDTO = {
      ...this.selectedUser,
      dob: this.formatDateForBackend(this.selectedUser.dob)
      // The address property is already part of this.selectedUser, so it's included by the spread operator.
    };

    this.userService.updateUser(this.selectedUser.userId, payload).subscribe({
      next: (updatedUser) => {
        this.successMessage = `User ${updatedUser.email} updated successfully`;
        this.loadUsers();
        this.selectedUser = undefined; // Clear the edit form
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Update error:', err);
        this.errorMessage = err.message || 'Failed to update user';
        this.isLoading = false;
      }
    });
  }

  private formatDateForBackend(dateString: string | undefined): string | undefined {
    if (!dateString) return undefined;
    try {
      const date = new Date(dateString);
      return date.toISOString().split('T')[0];
    } catch (e) {
      console.error('Date format error:', e);
      return undefined;
    }
  }

  deleteUser(userId: number) {
    if (!confirm('Are you sure you want to delete this user?')) return;

    this.isLoading = true;
    this.userService.deleteUser(userId).subscribe({
      next: () => {
        this.successMessage = 'User deleted successfully';
        this.loadUsers();
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to delete user';
        this.isLoading = false;
      }
    });
  }
}