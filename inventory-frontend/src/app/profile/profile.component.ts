// src/app/profile/profile.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AppUserDTO, AppUserService, OtpVerificationResponse } from '../services/app-user.service'; // Import OtpVerificationResponse
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
    MatTooltipModule,
  ]
})
export class ProfileComponent implements OnInit {
  userProfile: AppUserDTO | null = null;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  // New properties for email change with OTP
  emailChangeMode = false;
  newEmail = '';
  emailOtp = '';
  otpSentForEmail = false;
  otpVerifiedForEmail = false;
  originalEmail = ''; // To store the original email when changing

  constructor(
    private authService: AuthService,
    private userService: AppUserService
  ) {}

  ngOnInit(): void {
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    const userId = this.authService.getUserIdFromToken();

    if (userId) {
      this.userService.getUserById(userId).subscribe({
        next: (user: AppUserDTO) => {
          this.userProfile = { ...user };
          this.originalEmail = user.email; // Store original email

          // Ensure address is initialized even if null from backend
          if (!this.userProfile.address) {
            this.userProfile.address = {
              countryRegion: '',
              pincode: '',
              flatHouseNoBuildingCompanyApartment: '',
              areaStreetSectorVillage: '',
              landmark: '',
              townCity: '',
              state: ''
            };
          }

          if (this.userProfile.dob) {
            this.userProfile.dob = this.formatDateForInput(this.userProfile.dob);
          }
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = 'Failed to load profile: ' + (err.error?.message || err.message);
          this.isLoading = false;
          console.error('Error loading user profile:', err);
        }
      });
    } else {
      this.errorMessage = 'User not logged in or ID missing.';
      this.isLoading = false;
    }
  }

  toggleEmailChangeMode(): void {
    this.emailChangeMode = !this.emailChangeMode;
    if (this.emailChangeMode) {
      this.newEmail = this.userProfile?.email || ''; // Pre-fill with current email
      this.otpSentForEmail = false;
      this.otpVerifiedForEmail = false;
      this.emailOtp = '';
      this.errorMessage = '';
      this.successMessage = '';
    }
  }

  cancelEmailChange(): void {
    this.emailChangeMode = false;
    this.otpSentForEmail = false;
    this.otpVerifiedForEmail = false;
    this.emailOtp = '';
    this.newEmail = '';
    this.userProfile!.email = this.originalEmail; // Revert email field
    this.errorMessage = '';
    this.successMessage = '';
  }

  sendOtpForEmailChange(): void {
    if (!this.newEmail) {
      this.errorMessage = 'Please enter the new email address.';
      return;
    }
    if (this.newEmail === this.originalEmail) {
        this.errorMessage = 'New email must be different from the current email.';
        return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Use sendRegistrationOtp, assuming your backend reuses it for email updates
    this.userService.sendRegistrationOtp(this.newEmail).subscribe({
      next: (response: string) => {
        this.otpSentForEmail = true;
        this.successMessage = 'OTP sent to ' + this.newEmail + '. ' + response;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to send OTP for email change.';
        this.isLoading = false;
        console.error('Error sending OTP for email change:', err);
      }
    });
  }

  verifyOtpForEmailChange(): void {
    if (!this.emailOtp) {
      this.errorMessage = 'Please enter the OTP.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Use verifyOtp, assuming your backend reuses it for email updates
    this.userService.verifyOtp(this.newEmail, this.emailOtp).subscribe({
      next: (response: OtpVerificationResponse) => {
        this.otpVerifiedForEmail = response.verified;
        this.successMessage = response.message;
        this.isLoading = false;
        if (!response.verified) {
          this.errorMessage = 'OTP verification failed: ' + response.message;
        }
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to verify OTP for email change.';
        this.isLoading = false;
        console.error('Error verifying OTP for email change:', err);
      }
    });
  }

  updateProfile(): void {
    if (!this.userProfile || !this.userProfile.userId) {
      this.errorMessage = 'No profile to update.';
      return;
    }

    // Prevent update if email change mode is active but OTP is not verified
    if (this.emailChangeMode && !this.otpVerifiedForEmail) {
        this.errorMessage = 'Please verify the new email with OTP before updating.';
        return;
    }
    if (this.emailChangeMode && this.newEmail !== this.userProfile.email && !this.otpVerifiedForEmail) {
        this.errorMessage = 'New email not verified. Please verify OTP or cancel email change.';
        return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload: AppUserDTO = {
        ...this.userProfile,
        dob: this.formatDateForBackend(this.userProfile.dob)
    };

    // If email was changed and verified, update the email in the payload
    if (this.emailChangeMode && this.otpVerifiedForEmail) {
        payload.email = this.newEmail;
    } else {
        // Ensure the original email is sent if not changing email
        payload.email = this.originalEmail;
    }


    this.userService.updateUser(this.userProfile.userId, payload).subscribe({
      next: (updatedUser) => {
        this.successMessage = 'Profile updated successfully!';
        this.userProfile = { ...updatedUser };
        // Reset email change state after successful update
        this.originalEmail = updatedUser.email; // Update original email
        this.emailChangeMode = false;
        this.otpSentForEmail = false;
        this.otpVerifiedForEmail = false;
        this.emailOtp = '';
        this.newEmail = '';

        // Re-initialize address object after update in case it became null
        if (!this.userProfile.address) {
          this.userProfile.address = {
            countryRegion: '',
            pincode: '',
            flatHouseNoBuildingCompanyApartment: '',
            areaStreetSectorVillage: '',
            landmark: '',
            townCity: '',
            state: ''
          };
        }

        if (this.userProfile.dob) {
          this.userProfile.dob = this.formatDateForInput(this.userProfile.dob);
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to update profile: ' + (err.error?.message || err.message);
        this.isLoading = false;
        console.error('Error updating user profile:', err);
      }
    });
  }

  private formatDateForInput(dateString: string): string {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      // Ensure it's a valid date before formatting
      if (isNaN(date.getTime())) {
        console.warn('Invalid date string for input:', dateString);
        return dateString; // Return original if invalid
      }
      return date.toISOString().split('T')[0];
    } catch (e) {
      console.error('Error formatting date for input:', e);
      return dateString;
    }
  }

  private formatDateForBackend(dateString: string | undefined): string | undefined {
    if (!dateString) return undefined;
    try {
      const date = new Date(dateString);
      // Ensure it's a valid date before formatting
      if (isNaN(date.getTime())) {
        console.warn('Invalid date string for backend:', dateString);
        return undefined; // Or throw error, depending on desired behavior
      }
      return date.toISOString().split('T')[0];
    } catch (e) {
      console.error('Error formatting date for backend:', e);
      return undefined;
    }
  }
}