// src/app/services/theme.service.ts
import { Injectable, Renderer2, RendererFactory2, Inject, PLATFORM_ID } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

// Define theme types for better type safety
export type Theme = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private currentThemeSubject: BehaviorSubject<Theme>;
  public currentTheme$: Observable<Theme>;

  private renderer: Renderer2;
  private isBrowser: boolean; // Flag to check if running in browser

  constructor(
    rendererFactory: RendererFactory2,
    @Inject(PLATFORM_ID) private platformId: Object // Inject PLATFORM_ID
  ) {
    this.renderer = rendererFactory.createRenderer(null, null);
    this.isBrowser = isPlatformBrowser(this.platformId); // Determine if running in a browser

    let initialTheme: Theme = 'light'; // Default theme

    // Only access localStorage if running in a browser
    if (this.isBrowser) {
      const savedTheme = localStorage.getItem('appTheme') as Theme;
      initialTheme = savedTheme || 'light';
    }

    this.currentThemeSubject = new BehaviorSubject<Theme>(initialTheme);
    this.currentTheme$ = this.currentThemeSubject.asObservable();

    // Apply initial theme class (only if in browser, as renderer also interacts with DOM)
    if (this.isBrowser) {
      this.applyTheme(this.currentThemeSubject.getValue());
    }
  }

  getTheme(): Theme {
    return this.currentThemeSubject.getValue();
  }

  toggleTheme(): void {
    const newTheme: Theme = this.currentThemeSubject.getValue() === 'light' ? 'dark' : 'light';
    this.setTheme(newTheme);
  }

  setTheme(theme: Theme): void {
    // Only update if theme is different and in a browser
    if (this.isBrowser && this.currentThemeSubject.getValue() !== theme) {
      this.currentThemeSubject.next(theme);
      localStorage.setItem('appTheme', theme); // Save preference
      this.applyTheme(theme);
    } else if (!this.isBrowser) {
        // Log a warning or handle if a theme change is attempted during SSR
        console.warn('Theme change attempted during SSR. localStorage not available.');
    }
  }

  private applyTheme(theme: Theme): void {
    // Ensure this only runs in the browser
    if (this.isBrowser) {
      const body = document.body;
      if (theme === 'dark') {
        this.renderer.addClass(body, 'dark-theme');
        this.renderer.removeClass(body, 'light-theme');
      } else {
        this.renderer.removeClass(body, 'dark-theme');
        this.renderer.addClass(body, 'light-theme');
      }
    }
  }
}