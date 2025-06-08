import { TestBed } from '@angular/core/testing';
import { AuthGuard } from './auth.guard';  // Correct import for class-based guard
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { of } from 'rxjs';

// Mock AuthService
class MockAuthService {
  isAuthenticated() {
    return true;  // Simulate an authenticated user
  }
}

// Mock Router
class MockRouter {
  navigate() {}  // Do nothing for navigation
}

describe('AuthGuard', () => {
  let authGuard: AuthGuard;
  let authService: AuthService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useClass: MockAuthService },
        { provide: Router, useClass: MockRouter }
      ]
    });

    authGuard = TestBed.inject(AuthGuard);  // Get instance of the AuthGuard
    authService = TestBed.inject(AuthService);  // Get instance of the AuthService
    router = TestBed.inject(Router);  // Get instance of the Router
  });

  it('should be created', () => {
    expect(authGuard).toBeTruthy();  // Ensure AuthGuard is created
  });

  it('should allow navigation if the user is authenticated', () => {
    spyOn(authService, 'isAuthenticated').and.returnValue(true);  // Mock authenticated user

    const result = authGuard.canActivate();  // Call the canActivate method
    expect(result).toBeTrue();  // Should allow navigation
  });

  it('should prevent navigation and redirect to login if user is not authenticated', () => {
    spyOn(authService, 'isAuthenticated').and.returnValue(false);  // Mock non-authenticated user
    spyOn(router, 'navigate');  // Spy on the navigate method of the Router

    const result = authGuard.canActivate();  // Call the canActivate method
    expect(result).toBeFalse();  // Should prevent navigation
    expect(router.navigate).toHaveBeenCalledWith(['/login']);  // Should redirect to login
  });
});
