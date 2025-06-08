import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return false;
    }

    // Role-based check (optional on routes)
    const expectedRoles = route.data['roles'] as string[];
    if (expectedRoles && expectedRoles.length > 0) {
      if (!this.authService.hasAnyRole(expectedRoles)) {
        this.router.navigate(['/unauthorized']);
        return false;
      }
    }

    return true;
  }
}
