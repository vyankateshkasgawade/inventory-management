import { Component, OnInit } from '@angular/core'; // Import OnInit
import { ThemeService, Theme } from './services/theme.service'; // Import your ThemeService and Theme type

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: false,
  styleUrl: './app.component.css' 
})
export class AppComponent implements OnInit { 
  title = 'inventory-frontend';
  currentTheme: Theme = 'light'; 

  constructor(private themeService: ThemeService) { } // Inject the ThemeService

  ngOnInit(): void {
   
    this.themeService.currentTheme$.subscribe(theme => {
      this.currentTheme = theme;
      console.log('Current theme in AppComponent:', this.currentTheme);
    });
  }


  toggleTheme(): void {
    this.themeService.toggleTheme();
  }
}