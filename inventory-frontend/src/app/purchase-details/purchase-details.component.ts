import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

import { PurchaseDetails } from '../models/purchase-details.model';
import { PurchaseDetailsService } from '../services/purchase-details.service';
import { AuthService } from '../auth.service';



@Component({
  selector: 'app-purchase-details',
  standalone: true,
  templateUrl: './purchase-details.component.html',
  styleUrls: ['./purchase-details.component.css'],
  imports: [
    CommonModule,
    FormsModule,
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
    DatePipe, // Add DatePipe to imports for date formatting
  ]
})
export class PurchaseDetailsComponent implements OnInit {
  purchases: PurchaseDetails[] = [];
  loggedInUserId: number | null = null;
  isAdmin: boolean = false;

  selectedPurchase: PurchaseDetails = {
    userId: 0,
    productId: 0,
    productCount: 0
  };

  // Define column sets based on roles
  // Admin sees all relevant columns including ID and actions
  private adminDisplayedColumns: string[] = [
    'purchaseDetailsId', // Admin specific
    'userId',            // Admin specific
    'userName',          // Admin specific
    'productId',
    'productName',
    'productCount',
    'purchaseDate'
  ];

  // User sees only product details and purchase date
  private userDisplayedColumns: string[] = [
    'productName',
    'productId',
    'productCount',
    'purchaseDate'
  ];

  // This will be the actual array used by mat-table
  displayedColumns: string[] = [];

  constructor(
    private purchaseService: PurchaseDetailsService,
    private authService: AuthService // Inject AuthService
  ) {}

  ngOnInit(): void {
    // Subscribe to changes in user ID and roles
    this.authService.loggedInUser.subscribe(user => {
      if (user) {
        this.loggedInUserId = this.authService.getUserIdFromToken();
        // --- THIS IS THE CRUCIAL CHANGE ---
        // Change 'ADMIN' to 'ROLE_ADMIN' to match your backend's JWT role
        this.isAdmin = this.authService.hasRole('ROLE_ADMIN');
        // --- END OF CRUCIAL CHANGE ---

        console.log('PurchaseDetailsComponent: LoggedInUserId:', this.loggedInUserId, 'IsAdmin (after check):', this.isAdmin);
      } else {
        this.loggedInUserId = null;
        this.isAdmin = false;
        console.log('PurchaseDetailsComponent: User logged out/not logged in.');
      }
      this.updateDisplayedColumns(); // Update columns whenever authentication state changes
      this.loadPurchases(); // Load purchases whenever authentication state changes
    });
  }

  private updateDisplayedColumns(): void {
    this.displayedColumns = this.isAdmin ? this.adminDisplayedColumns : this.userDisplayedColumns;
    console.log('PurchaseDetailsComponent: Displayed columns set to:', this.displayedColumns); // Added log
  }

  loadPurchases(): void {
    if (this.isAdmin) {
      // Admin sees all purchases
      this.purchaseService.getAllPurchases().subscribe(data => {
        this.purchases = data;
        console.log('PurchaseDetailsComponent: Admin loading all purchases.', this.purchases); // Added log
      }, error => {
        console.error('Error fetching all purchases:', error);
        // Implement user-friendly error display here
      });
    } else if (this.loggedInUserId) {
      // Regular user sees only their own purchases
      this.purchaseService.getPurchasesByUserId(this.loggedInUserId).subscribe(data => {
        this.purchases = data;
        console.log(`PurchaseDetailsComponent: User ${this.loggedInUserId} loading their purchases.`, this.purchases); // Added log
      }, error => {
        console.error(`Error fetching purchases for user ${this.loggedInUserId}:`, error);
        // Implement user-friendly error display here
      });
    } else {
      // If no user is logged in, clear purchases
      this.purchases = [];
      console.log('No user logged in, clearing purchase list.');
    }
  }

  save(): void {
    // This form section is commented out in HTML, but logic is kept for completeness.
    // If it's a new purchase and a user is logged in, ensure userId is set
    if (!this.selectedPurchase.purchaseDetailsId && this.loggedInUserId) {
      this.selectedPurchase.userId = this.loggedInUserId;
    }

    if (this.selectedPurchase.purchaseDetailsId) {
      this.purchaseService
        .updatePurchase(this.selectedPurchase.purchaseDetailsId, this.selectedPurchase)
        .subscribe(() => {
          this.loadPurchases();
          this.resetForm();
        }, error => {
          console.error('Error updating purchase:', error);
          // Implement user-friendly error display here
        });
    } else {
      this.purchaseService
        .createPurchase(this.selectedPurchase)
        .subscribe(() => {
          this.loadPurchases();
          this.resetForm();
        }, error => {
          console.error('Error creating purchase:', error);
          // Implement user-friendly error display here
        });
    }
  }

  edit(purchase: PurchaseDetails): void {
    // This method is only called if the 'edit' button is visible (i.e., admin or owner)
    this.selectedPurchase = { ...purchase };
  }

  delete(id: number): void {
    // Optional: Add a confirmation dialog here
    if (confirm('Are you sure you want to delete this purchase?')) {
      this.purchaseService.deletePurchase(id).subscribe(() => {
        this.loadPurchases();
      }, error => {
        console.error('Error deleting purchase:', error);
        // Implement user-friendly error display here
      });
    }
  }

  resetForm(): void {
    // This form section is commented out in HTML, but logic is kept for completeness.
    this.selectedPurchase = {
      userId: this.loggedInUserId || 0, // Pre-fill with logged-in user ID, or 0 if no user
      productId: 0,
      productCount: 0
    };
  }

  trackByPurchaseDetailsId(index: number, item: PurchaseDetails): number | undefined {
    return item.purchaseDetailsId;
  }
}