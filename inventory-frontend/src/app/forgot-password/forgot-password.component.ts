// src/app/forgot-password/forgot-password.component.ts
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../auth.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterModule
  ],
  templateUrl: './forgot-password.component.html', // We'll modify this HTML soon
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  // Forms for each step
  forgotPasswordForm: FormGroup; // For email
  otpVerificationForm: FormGroup; // For OTP
  resetPasswordForm: FormGroup;   // For new password

  errorMessage: string | null = null;
  successMessage: string | null = null;
  currentStep: 'email' | 'otp' | 'reset' = 'email'; // Control which step is visible
  emailValue: string = ''; // Store email across steps

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });

    this.otpVerificationForm = this.fb.group({
      otp: ['', [Validators.required, Validators.pattern('^[0-9]{6}$')]] // Assuming 6-digit OTP
    });

    this.resetPasswordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]], // Min 6 chars for password
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator }); // Add password match validator

  }

  // Custom validator for password matching
  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { mismatch: true };
  }


  // --- Step 1: Send OTP (Email input) ---
  sendOtp(): void {
    this.errorMessage = null;
    this.successMessage = null;

    if (this.forgotPasswordForm.invalid) {
      this.forgotPasswordForm.markAllAsTouched();
      return;
    }

    const { email } = this.forgotPasswordForm.value;
    this.emailValue = email; // Store email for later steps

    this.authService.forgotPassword({ email }).subscribe({
      next: (response) => {
        this.successMessage = response; // Display the message from backend
        this.currentStep = 'otp'; // Move to OTP verification step
        this.otpVerificationForm.reset(); // Clear previous OTP attempts
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to send reset email. Please try again.';
      }
    });
  }

  // --- Step 2: Verify OTP ---
  verifyOtp(): void {
    this.errorMessage = null;
    this.successMessage = null;

    if (this.otpVerificationForm.invalid) {
      this.otpVerificationForm.markAllAsTouched();
      return;
    }

    const { otp } = this.otpVerificationForm.value;

    this.authService.verifyOtp({ email: this.emailValue, otp }).subscribe({
      next: (response) => {
        this.successMessage = response; // "OTP verified successfully."
        this.currentStep = 'reset'; // Move to Reset Password step
        this.resetPasswordForm.reset(); // Clear previous password attempts
      },
      error: (err) => {
        this.errorMessage = err.message || 'Invalid OTP. Please try again.';
      }
    });
  }

  // --- Step 3: Reset Password ---
  resetPassword(): void {
    this.errorMessage = null;
    this.successMessage = null;

    if (this.resetPasswordForm.invalid) {
      this.resetPasswordForm.markAllAsTouched();
      return;
    }

    if (this.resetPasswordForm.errors?.['mismatch']) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    const { newPassword, confirmPassword } = this.resetPasswordForm.value;
    const { otp } = this.otpVerificationForm.value; // Get OTP from the previous form

    this.authService.resetPassword({ email: this.emailValue, otp: otp, newPassword: newPassword }).subscribe({
      next: (response) => {
        this.successMessage = response; // "Password reset successful."
        this.currentStep = 'email'; // Optionally go back to email step, or redirect to login
        // this.router.navigate(['/login']); // <-- More common to redirect to login
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to reset password. Please try again.';
      }
    });
  }

  // Helper to check if a control has a specific error
  hasError(formGroup: FormGroup, controlName: string, errorType: string) {
    const control = formGroup.get(controlName);
    return control?.touched && control?.errors?.[errorType];
  }

  // Helper to check if passwords mismatch
  passwordsMismatch() {
    return this.resetPasswordForm.hasError('mismatch') &&
           (this.resetPasswordForm.get('newPassword')?.touched ||
            this.resetPasswordForm.get('confirmPassword')?.touched);
  }
}