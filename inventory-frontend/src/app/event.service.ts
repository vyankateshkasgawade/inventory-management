import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { User } from './models/user.model';

const LOCAL_STORAGE_USER_KEY = 'loggedInUser';
const LOCAL_STORAGE_TOKEN_KEY = 'authToken';

@Injectable({
  providedIn: 'root'
})
export class EventService {

  // BehaviorSubject to store the logged-in user
  private loggedInUserSubject = new BehaviorSubject<User | null>(this.getStoredUser());

  // Observable for components to subscribe to
  public loggedInUser$ = this.loggedInUserSubject.asObservable();

  constructor() {}

  /**
   * Logs in the user by storing user and token in localStorage
   * and updating the subject.
   */
  login(user: User, token: string): void {
    localStorage.setItem(LOCAL_STORAGE_USER_KEY, JSON.stringify(user));
    localStorage.setItem(LOCAL_STORAGE_TOKEN_KEY, token);
    this.loggedInUserSubject.next(user);
  }

  /**
   * Logs out the user by clearing storage and resetting subject.
   */
  logout(): void {
    localStorage.removeItem(LOCAL_STORAGE_USER_KEY);
    localStorage.removeItem(LOCAL_STORAGE_TOKEN_KEY);
    this.loggedInUserSubject.next(null);
  }

  /**
   * Returns the current user (non-observable version).
   */
  getCurrentUser(): User | null {
    return this.loggedInUserSubject.value;
  }

  /**
   * Returns the current token from localStorage.
   */
  getToken(): string | null {
    return localStorage.getItem(LOCAL_STORAGE_TOKEN_KEY);
  }

  /**
   * Helper to load user from localStorage on service init.
   */
  private getStoredUser(): User | null {
    const userJson = localStorage.getItem(LOCAL_STORAGE_USER_KEY);
    return userJson ? JSON.parse(userJson) : null;
  }
}

