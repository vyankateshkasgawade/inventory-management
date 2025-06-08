// src/app/register/register.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router'; // Import Router
import { AppUserService, RegistrationRequest, AddressDTO } from '../services/app-user.service'; // Import AppUserService and DTOs

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterModule
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  isLoading = false;
  otpSent = false;
  otpVerified = false;

  constructor(
    private fb: FormBuilder,
    private appUserService: AppUserService, // Use AppUserService directly for registration
    private router: Router
  ) {
    // Initialize the form with all fields and validators
    this.registerForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      phone: ['', Validators.required],
      dob: [''], // Date of Birth - optional or add specific date validation
      otp: [''], // OTP field, only required when otpSent is true

      address: this.fb.group({ // Nested FormGroup for Address
        countryRegion: [''],
        pincode: [''],
        flatHouseNoBuildingCompanyApartment: [''],
        areaStreetSectorVillage: [''],
        landmark: [''],
        townCity: [''],
        state: ['']
      })
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    // Disable OTP and address fields initially if needed,
    // or let template *ngIf handle visibility.
    // For reactive forms, you can enable/disable controls dynamically.
    this.registerForm.get('otp')?.disable();
    this.registerForm.get('address')?.enable(); // Ensure address is enabled
  }

  // Custom validator for password match
  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { 'mismatch': true };
  }

  // Step 1: Send OTP for registration
  sendOtp(): void {
    this.errorMessage = null;
    this.successMessage = null;

    // Validate only the user details fields before sending OTP
    const { name, email, password, confirmPassword, phone } = this.registerForm.value;
    if (!name || !email || !password || !confirmPassword || !phone) {
        this.registerForm.markAllAsTouched(); // Mark all touched to show errors
        this.errorMessage = 'Please fill in all required user details.';
        return;
    }

    if (this.registerForm.errors?.['mismatch']) {
        this.errorMessage = 'Passwords do not match.';
        return;
    }

    this.isLoading = true;
    this.appUserService.sendRegistrationOtp(email).subscribe({
      next: (response: string) => {
        this.successMessage = response;
        this.otpSent = true;
        this.isLoading = false;
        this.registerForm.get('otp')?.enable(); // Enable OTP input after sending
        // Optionally, disable other fields now that OTP is sent
        this.registerForm.get('name')?.disable();
        this.registerForm.get('email')?.disable();
        this.registerForm.get('password')?.disable();
        this.registerForm.get('confirmPassword')?.disable();
        this.registerForm.get('phone')?.disable();
        this.registerForm.get('dob')?.disable();
        this.registerForm.get('address')?.disable();
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to send OTP.';
        this.isLoading = false;
      }
    });
  }

  // Step 2: Verify OTP
  verifyOtp(): void {
    this.errorMessage = null;
    this.successMessage = null;

    const email = this.registerForm.get('email')?.value;
    const otp = this.registerForm.get('otp')?.value;

    if (!otp) {
      this.errorMessage = 'Please enter the OTP.';
      this.registerForm.get('otp')?.markAsTouched();
      return;
    }

    this.isLoading = true;
    this.appUserService.verifyOtp(email, otp).subscribe({
      next: (response) => {
        if (response.verified) {
          this.otpVerified = true;
          this.successMessage = response.message;
          // You might want to disable the OTP field now
          this.registerForm.get('otp')?.disable();
        } else {
          this.errorMessage = response.message || 'Invalid OTP code.';
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to verify OTP.';
        this.isLoading = false;
      }
    });
  }

  // Step 3: Complete Registration (onSubmit is now renamed to registerUser to match AppUserComponent)
  // We will call this method only when otpVerified is true.
  onSubmit(): void {
    if (!this.otpVerified) {
      this.errorMessage = 'Please verify OTP first.';
      return;
    }

    // Ensure all necessary fields are re-enabled for submission if they were disabled
    this.registerForm.get('name')?.enable();
    this.registerForm.get('email')?.enable();
    this.registerForm.get('password')?.enable();
    this.registerForm.get('confirmPassword')?.enable();
    this.registerForm.get('phone')?.enable();
    this.registerForm.get('dob')?.enable();
    this.registerForm.get('address')?.enable();
    this.registerForm.get('otp')?.enable(); // Ensure OTP is part of payload for backend

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      this.errorMessage = 'Please correct the form errors.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;

    const registrationData: RegistrationRequest = {
      name: this.registerForm.value.name,
      email: this.registerForm.value.email,
      password: this.registerForm.value.password,
      otp: this.registerForm.value.otp, // Include OTP in the final registration request
      phone: this.registerForm.value.phone,
      dob: this.registerForm.value.dob ? this.formatDateForBackend(this.registerForm.value.dob) : undefined, // Format DOB
      role: 'USER', // Default role
      address: this.registerForm.value.address // Address object
    };

    this.appUserService.registerUser(registrationData).subscribe({
      next: (response: string) => {
        this.successMessage = response + ' Redirecting to login...';
        this.isLoading = false;
        this.registerForm.reset(); // Clear the form
        // Optional: Redirect to login page after successful registration
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 3000); // Redirect after 3 seconds
      },
      error: (err) => {
        console.error('Registration failed:', err);
        this.errorMessage = err.message || 'Registration failed. Please try again.';
        this.isLoading = false;
      }
    });
  }

  private formatDateForBackend(dateString: string): string | undefined {
    if (!dateString) return undefined;
    try {
      const date = new Date(dateString);
      // Ensure the format matches what your backend expects (e.g., "YYYY-MM-DD")
      return date.toISOString().split('T')[0];
    } catch (e) {
      console.error('Date format error for backend:', e);
      return undefined;
    }
  }

  // Function to reset the form state
  resetFormState(): void {
    this.registerForm.reset();
    this.otpSent = false;
    this.otpVerified = false;
    this.isLoading = false;
    this.errorMessage = null;
    this.successMessage = null;
    this.registerForm.get('otp')?.disable(); // Disable OTP field on reset
  }
}