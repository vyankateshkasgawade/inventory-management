// src/app/dashboard/dashboard.component.ts

import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatBadgeModule } from '@angular/material/badge';
import { MatSidenav } from '@angular/material/sidenav';

import { AuthService } from '../auth.service';
import { ProductDetailsService } from '../services/product-details.service';
import { ProductDetailsDTO } from '../dto/product-details-dto';
import { ProductCategoryService, ProductCategory } from '../services/product-category.service';
import { environment } from '../../environments/environment';
import { CartService } from '../services/cart.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CartDTO } from '../models/cart.model';
import { Subject, takeUntil } from 'rxjs';

// NEW IMPORTS FOR WISHLIST
import { WishlistService } from '../services/wishlist.service';
import { WishlistDTO } from '../models/wishlist-dto';
import { HttpErrorResponse } from '@angular/common/http';

// Angular Material Modules
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';

interface ProductsByCategory {
  categoryName: string;
  products: ProductDetailsDTO[];
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatDividerModule,
    CurrencyPipe,
    MatBadgeModule,
    MatExpansionModule,
    MatSelectModule,
    MatFormFieldModule
  ]
})
export class DashboardComponent implements OnInit, OnDestroy {
  products: ProductDetailsDTO[] = [];
  productsGroupedByCategory: ProductsByCategory[] = [];
  topTrendingProducts: ProductDetailsDTO[] = [];
  topSellingProducts: ProductDetailsDTO[] = [];
  backendBaseUrl = environment.apiUrl;
  selectedProduct: ProductDetailsDTO | null = null;
  userId: number | null = null;
  cartItemCount: number = 0;

  categories: ProductCategory[] = [];
  selectedCategoryName: string | null = null;

  private destroy$ = new Subject<void>();

  @ViewChild('sidenav') sidenav!: MatSidenav;

  // WISHLIST PROPERTIES
  wishlistEntries: WishlistDTO[] = [];
  userWishlistProductIds: Set<number> = new Set();
  isLoggedIn: boolean = false;

  constructor(
    private router: Router,
    public authService: AuthService,
    private productService: ProductDetailsService,
    private productCategoryService: ProductCategoryService,
    private snackBar: MatSnackBar,
    private cartService: CartService,
    private wishlistService: WishlistService // Inject WishlistService
  ) {}

  ngOnInit(): void {
    this.verifyUserAndLoadProducts();
    this.loadCategories();
    this.loadTopTrendingProducts();
    this.loadTopSellingProducts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  toggleSidenav(): void {
    if (this.sidenav) {
      this.sidenav.toggle();
    }
  }

  verifyUserAndLoadProducts(): void {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const base64Payload = token.split('.')[1];
        const payloadJson = atob(base64Payload);
        const payload = JSON.parse(payloadJson);
        this.userId = Number(payload?.userId || payload?.sub || null);

        if (!isNaN(this.userId) && this.userId > 0) {
          console.log('Authenticated userId:', this.userId);
          this.isLoggedIn = true;
          if (this.isHomeRoute()) {
            this.loadProducts(this.selectedCategoryName);
          }
          this.loadCartItemCount();
          this.loadUserWishlist(); // Load wishlist once user is verified
        } else {
          console.error('Invalid user ID in token. Redirecting to login.');
          this.router.navigate(['/login']);
          this.isLoggedIn = false;
        }
      } catch (error) {
        console.error('Error parsing token:', error);
        this.snackBar.open('Authentication error. Please log in again.', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
        this.router.navigate(['/login']);
        this.isLoggedIn = false;
      }
    } else {
      console.warn('No token found. Redirecting to login.');
      this.router.navigate(['/login']);
      this.isLoggedIn = false;
    }
  }

  isHomeRoute(): boolean {
    return this.router.url === '/dashboard' || this.router.url === '/';
  }

  isAdmin(): boolean {
    return this.authService.hasRole('ROLE_ADMIN');
  }

  loadProducts(categoryName: string | null = null): void {
    this.productService.getAllProducts().subscribe(
      data => {
        const activeProducts = data.filter(p => p.isActive);
        if (categoryName && categoryName !== 'All') {
          this.products = activeProducts.filter(p => p.productCategoryName === categoryName);
        } else {
          this.products = activeProducts;
        }
        this.groupProductsByCategory();
        console.log("Products loaded successfully for dashboard:", this.products);
        console.log("Products grouped by category:", this.productsGroupedByCategory);
        // After loading products, if wishlist is also loaded, update status
        if (this.isLoggedIn && this.wishlistEntries.length > 0) {
          this.updateWishlistStatusForProducts();
        }
      },
      error => {
        console.error('Error loading products for dashboard:', error);
        this.snackBar.open('Failed to load products. Please try again.', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    );
  }

  loadCategories(): void {
    this.productCategoryService.getAllCategories().subscribe({
      next: (data) => {
        this.categories = data;
        if (!this.categories.some(cat => cat.productCategoryName === 'All')) {
          this.categories.unshift({ productCategoryName: 'All', productCategoryId: 0 });
        }
        this.selectedCategoryName = 'All';
        this.loadProducts(this.selectedCategoryName);
      },
      error: (err) => {
        console.error('Error loading product categories:', err);
        this.snackBar.open('Failed to load categories. Please try again.', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  onCategoryChange(): void {
    this.loadProducts(this.selectedCategoryName);
  }

  groupProductsByCategory(): void {
    const grouped: { [key: string]: ProductDetailsDTO[] } = {};
    this.products.forEach(product => {
      const category = product.productCategoryName || 'Uncategorized';
      if (!grouped[category]) {
        grouped[category] = [];
      }
      grouped[category].push(product);
    });

    this.productsGroupedByCategory = Object.keys(grouped).map(categoryName => ({
      categoryName,
      products: grouped[categoryName]
    }));

    this.productsGroupedByCategory.sort((a, b) => a.categoryName.localeCompare(b.categoryName));

    const allCategoryIndex = this.productsGroupedByCategory.findIndex(group => group.categoryName === 'All');
    if (allCategoryIndex > 0) {
      const [allCategory] = this.productsGroupedByCategory.splice(allCategoryIndex, 1);
      this.productsGroupedByCategory.unshift(allCategory);
    }
  }

  loadTopTrendingProducts(): void {
    this.productService.getTopTrendingProducts().subscribe({
      next: (data) => {
        this.topTrendingProducts = data.filter(p => p.isActive);
        console.log('Top Trending Products:', this.topTrendingProducts);
        if (this.isLoggedIn && this.wishlistEntries.length > 0) {
          this.updateWishlistStatusForProducts();
        }
      },
      error: (err) => {
        console.error('Error loading top trending products:', err);
        this.snackBar.open('Failed to load top trending products.', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      }
    });
  }

  loadTopSellingProducts(): void {
    this.productService.getTopSellingProducts().subscribe({
      next: (data) => {
        this.topSellingProducts = data.filter(p => p.isActive);
        console.log('Top Selling Products:', this.topSellingProducts);
        if (this.isLoggedIn && this.wishlistEntries.length > 0) {
          this.updateWishlistStatusForProducts();
        }
      },
      error: (err) => {
        console.error('Error loading top selling products:', err);
        this.snackBar.open('Failed to load top selling products.', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      }
    });
  }

  loadCartItemCount(): void {
    if (this.userId !== null) {
      this.cartService.getCartByUserId(this.userId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (cartItems: CartDTO[]) => {
            this.cartItemCount = cartItems.reduce((sum, item) => sum + (item.isActive ? item.quantity : 0), 0);
            console.log('Cart item count:', this.cartItemCount);
          },
          error: (error) => {
            console.error('Error loading cart item count:', error);
            this.cartItemCount = 0;
          }
        });
    } else {
      this.cartItemCount = 0;
    }
  }

  getProductImageUrl(imageUrl: string | null | undefined): string {
    if (imageUrl) {
      if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
        return imageUrl;
      }
      return this.backendBaseUrl + imageUrl;
    }
    return 'assets/placeholder-image.png';
  }

  openProductOptions(product: ProductDetailsDTO): void {
    this.selectedProduct = product;
  }

  closeProductOptions(): void {
    this.selectedProduct = null;
  }

  addToCart(product: ProductDetailsDTO): void {
    if (this.userId === null) {
      this.snackBar.open('You must be logged in to add items to cart.', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      this.router.navigate(['/login']);
      return;
    }

    if (!product.productDetailsId) {
      this.snackBar.open('Product ID is missing, cannot add to cart.', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      return;
    }

    const cartItem: CartDTO = {
      userId: this.userId,
      productId: product.productDetailsId,
      productName: product.productName,
      quantity: 1,
      price: product.price,
      imageUrl: product.imageUrl
    };

    this.cartService.addItemToCart(cartItem).subscribe({
      next: (response) => {
        console.log('Product added to cart:', response);
        this.snackBar.open(`${product.productName} added to cart successfully!`, 'Close', {
          duration: 2000,
          panelClass: ['success-snackbar']
        });
        this.closeProductOptions();
        this.loadCartItemCount();
      },
      error: (error) => {
        console.error('Error adding product to cart:', error);
        const errorMessage = error.error?.message || 'Failed to add item to cart.';
        this.snackBar.open(errorMessage, 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  buyNow(product: ProductDetailsDTO): void {
    console.log('Initiating buy now for:', product.productName);
    this.closeProductOptions();

    if (this.userId === null) {
      console.warn('User not logged in. Redirecting to login for direct buy.');
      this.router.navigate(['/login']);
      return;
    }

    if (product.productDetailsId) {
      this.router.navigate(
        ['/payment', 'product', product.productDetailsId],
        { queryParams: { userId: this.userId } }
      );
      console.log(`DashboardComponent: Navigating to /payment/product/${product.productDetailsId} with userId ${this.userId} in queryParams.`);
    } else {
      console.error('Product ID is missing for buy now action. Cannot navigate to payment.');
      this.snackBar.open('Error: Product ID missing for purchase.', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
    }
  }

  // WISHLIST METHODS

  loadUserWishlist(): void {
    if (this.userId === null) {
      console.warn('User ID is null. Cannot load wishlist.');
      return;
    }
    console.log('[DashboardComponent] Loading user wishlist for user ID:', this.userId);
    this.wishlistService.getWishlistByUserId(this.userId).subscribe({
      next: (wishlistItems: WishlistDTO[]) => {
        this.wishlistEntries = wishlistItems;
        this.updateWishlistStatusForProducts();
        console.log('Wishlist loaded:', this.wishlistEntries);
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error loading wishlist:', err);
        if (err.status !== 404) {
          this.snackBar.open(`Failed to load wishlist: ${err.error?.error || err.message}`, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
        }
      }
    });
  }

  addToWishlist(productId: number): void {
    if (this.userId === null) {
      this.snackBar.open('You must be logged in to add items to your wishlist.', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      this.router.navigate(['/login']);
      return;
    }
    console.log('[DashboardComponent] Attempting to add product to wishlist:', productId);
    this.wishlistService.addToWishlist(this.userId, productId).subscribe({
      next: (response) => {
        console.log('Product added to wishlist, response:', response);
        this.loadUserWishlist();
        this.snackBar.open('Product added to wishlist!', 'Close', { duration: 2000, panelClass: ['success-snackbar'] });
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error adding to wishlist:', err);
        let errorMessage = 'Failed to add to wishlist.';
        if (err.status === 409) {
          errorMessage = 'Product is already in your wishlist!';
        } else if (err.error && typeof err.error === 'string') {
          errorMessage = `Failed to add to wishlist: ${err.error}`;
        }
        this.snackBar.open(errorMessage, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      }
    });
  }

  removeFromWishlist(wishlistId: number): void {
    console.log('[DashboardComponent] Attempting to remove from wishlist:', wishlistId);
    this.wishlistService.removeFromWishlist(wishlistId).subscribe({
      next: () => {
        console.log('Product removed from wishlist.');
        this.loadUserWishlist();
        this.snackBar.open('Product removed from wishlist!', 'Close', { duration: 2000, panelClass: ['success-snackbar'] });
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error removing from wishlist:', err);
        this.snackBar.open(`Failed to remove from wishlist: ${err.error?.error || err.message}`, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      }
    });
  }

  isProductInWishlist(productId: number): boolean {
    return this.userWishlistProductIds.has(productId);
  }

  getWishlistIdForProduct(productId: number): number | undefined {
    const wishlistEntry = this.wishlistEntries.find(entry => entry.productId === productId);
    return wishlistEntry ? wishlistEntry.wishlistId : undefined;
  }

  private updateWishlistStatusForProducts(): void {
    this.userWishlistProductIds.clear();
    this.wishlistEntries.forEach(entry => {
      if (entry.productId) {
        this.userWishlistProductIds.add(entry.productId);
      }
    });
    console.log('Updated wishlist product IDs:', Array.from(this.userWishlistProductIds));
  }
}