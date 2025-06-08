import { Component, OnInit } from '@angular/core';
import { ProductDetailsDTO } from '../dto/product-details-dto';
import { ProductDetailsService, ProductOrderUpdateDTO } from '../services/product-details.service';
import { ProductCategoryService, ProductCategory } from '../services/product-category.service';
import { FormsModule } from '@angular/forms';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatIconModule } from '@angular/material/icon';
import { HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { Observable } from 'rxjs';
import { MatListModule } from '@angular/material/list';
import { MatSnackBar } from '@angular/material/snack-bar';

// --- NEW IMPORTS FOR WISHLIST ---
import { WishlistService } from '../services/wishlist.service';
import { WishlistDTO } from '../models/wishlist-dto';
// Make sure you have your AuthService or a way to get the current user ID
// import { AuthService } from '../services/auth.service';
// -----------------------------

@Component({
  standalone: true,
  selector: 'app-product-details',
  templateUrl: './product-details.component.html',
  styleUrls: ['./product-details.component.css'],
  imports: [
    FormsModule,
    CommonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatDividerModule,
    MatTooltipModule,
    MatIconModule,
    CurrencyPipe,
    MatCardModule,
    MatCheckboxModule,
    MatListModule
  ],
  providers: []
})
export class ProductDetailsComponent implements OnInit {
  showAddForm: boolean = false;
  products: ProductDetailsDTO[] = [];
  categories: ProductCategory[] = [];
  selectedFilter: string | number = 'all';

  newProduct: ProductDetailsDTO = {
    productName: '',
    productQuantity: 0,
    price: 0,
    productCategoryId: 0,
    isActive: true,
    imageUrl: null,
    trendingDisplayOrder: null,
    sellingDisplayOrder: null
  };
  editingProductId: number | null = null;
  updatedProduct: ProductDetailsDTO = {
    productDetailsId: undefined,
    productName: '',
    productQuantity: 0,
    price: 0,
    productCategoryId: 0,
    isActive: true,
    imageUrl: null,
    trendingDisplayOrder: null,
    sellingDisplayOrder: null
  };

  selectedNewFile: File | null = null;
  newImagePreviewUrl: string | null = null;
  selectedEditFile: File | null = null;
  editImagePreviewUrl: string | null = null;

  backendBaseUrl = environment.apiUrl;

  topTrendingProducts: ProductDetailsDTO[] = [];
  topSellingProducts: ProductDetailsDTO[] = [];

  filteredProductsForTrending: ProductDetailsDTO[] = [];
  filteredProductsForSelling: ProductDetailsDTO[] = [];
  trendingProductFilter: string = '';
  sellingProductFilter: string = '';

  // --- WISHLIST PROPERTIES ---
  wishlistEntries: WishlistDTO[] = []; // Stores the raw WishlistDTO objects from the backend
  userWishlistProductIds: Set<number> = new Set(); // Stores product IDs in wishlist for quick lookup

  // !!! IMPORTANT: Replace these with actual authenticated user ID and login status !!!
  // You should get these from your AuthService or similar authentication mechanism
  currentUserId: number = 1; // Example: Assuming user ID 1 for now
  isLoggedIn: boolean = true; // Example: Assuming user is logged in for now
  // -------------------------

  constructor(
    private productService: ProductDetailsService,
    private categoryService: ProductCategoryService,
    private snackBar: MatSnackBar,
    // private authService: AuthService, // Uncomment and inject your AuthService
    // --- INJECT WISHLIST SERVICE ---
    private wishlistService: WishlistService
    // -----------------------------
  ) {
    console.log('ProductDetailsComponent constructor called!');
  }

  ngOnInit(): void {
    console.log('ProductDetailsComponent ngOnInit called!');
    this.loadCategories(); // Categories might be needed for new product form
    this.loadProducts(); // Load products first, as wishlist display depends on them

    // --- LOAD WISHLIST ON INIT ---
    // Make sure to get actual user ID and login status here
    // this.currentUserId = this.authService.getCurrentUserId();
    // this.isLoggedIn = this.authService.isLoggedIn();
    if (this.isLoggedIn && this.currentUserId) {
      this.loadUserWishlist();
    }
    // ---------------------------
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

  loadProducts(): void {
    console.log('[ProductDetailsComponent] Loading all products...');
    this.productService.getAllProducts().subscribe({
      next: data => {
        this.products = data;
        console.log('All products loaded:', this.products);
        this.applyFilter();
        this.loadAllProductsForManagement();
        // After loading products, if wishlist is also loaded, update status
        if (this.isLoggedIn && this.wishlistEntries.length > 0) { // Check if wishlist was already loaded
          this.updateWishlistStatusForProducts();
        }
      },
      error: error => {
        console.error('Error loading all products:', error);
        this.snackBar.open('Failed to load all products. Check console for details.', 'Close', { duration: 3000 });
      }
    });
  }

  loadAllProductsForManagement(): void {
    // Deep clone to ensure independent modification in management sections
    this.filteredProductsForTrending = JSON.parse(JSON.stringify(this.products));
    this.filteredProductsForSelling = JSON.parse(JSON.stringify(this.products));

    this.filterTrendingProducts();
    this.filterSellingProducts();
  }

  loadCategories(): void {
    console.log('[ProductDetailsComponent] Loading categories...');
    this.categoryService.getAllCategories().subscribe({
      next: data => {
        this.categories = data;
        if (this.categories.length > 0 && !this.newProduct.productCategoryId) {
          this.newProduct.productCategoryId = this.categories[0].productCategoryId!;
        }
      },
      error: error => {
        console.error('Error loading categories:', error);
        this.snackBar.open('Failed to load categories. Check console for details.', 'Close', { duration: 3000 });
      }
    });
  }

  applyFilter(): void {
    let productsObservable: Observable<ProductDetailsDTO[]>;

    if (this.selectedFilter === 'all') {
      productsObservable = this.productService.getAllProducts();
    } else if (this.selectedFilter === 'trending') {
      productsObservable = this.productService.getTopTrendingProducts();
    } else if (this.selectedFilter === 'selling') {
      productsObservable = this.productService.getTopSellingProducts();
    } else if (typeof this.selectedFilter === 'number') {
      productsObservable = this.productService.getProductsByCategory(this.selectedFilter);
    } else {
      productsObservable = this.productService.getAllProducts(); // Default
    }

    productsObservable.subscribe({
      next: data => {
        this.products = data;
        console.log(`Products loaded for filter '${this.selectedFilter}':`, this.products);
        if (this.products.length === 0) {
            console.warn(`No products found for filter: ${this.selectedFilter}`);
        }
        if (this.isLoggedIn && this.wishlistEntries.length > 0) { // Check if wishlist was already loaded
          this.updateWishlistStatusForProducts();
        }
      },
      error: error => {
        console.error(`Error loading products for filter '${this.selectedFilter}':`, error);
        this.snackBar.open(`Failed to load products for this filter. Check console for details.`, 'Close', { duration: 3000 });
      }
    });
  }

  onNewFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      this.selectedNewFile = file;
      this.previewImage(file, 'new');
    } else {
      this.selectedNewFile = null;
      this.newImagePreviewUrl = null;
    }
  }

  onEditFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      this.selectedEditFile = file;
      this.previewImage(file, 'edit');
    } else {
      this.selectedEditFile = null;
      this.editImagePreviewUrl = null;
    }
  }

  previewImage(file: File, type: 'new' | 'edit'): void {
    const reader = new FileReader();
    reader.onload = () => {
      if (type === 'new') {
        this.newImagePreviewUrl = reader.result as string;
      } else {
        this.editImagePreviewUrl = reader.result as string;
      }
    };
    reader.readAsDataURL(file);
  }

  clearEditImage(): void {
    this.selectedEditFile = null;
    this.editImagePreviewUrl = null;
    this.updatedProduct.imageUrl = null;
    const fileInput = document.getElementById(`editProductImage-${this.editingProductId}`) as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  createProduct(): void {
    if (this.validateProduct(this.newProduct)) {
      this.productService.createProduct(this.newProduct, this.selectedNewFile).subscribe({
        next: () => {
          this.loadProducts();
          this.resetNewProductForm();
          this.showAddForm = false;
          this.snackBar.open('Product created successfully!', 'Close', { duration: 2000, panelClass: ['success-snackbar'] });
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error creating product:', error);
          this.snackBar.open(`Failed to create product: ${error.error?.error || error.message}`, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
        }
      });
    }
  }

  startEdit(product: ProductDetailsDTO): void {
    this.editingProductId = product.productDetailsId!;
    this.updatedProduct = { ...product };
    this.editImagePreviewUrl = null;
    this.selectedEditFile = null;
  }

  updateProduct(): void {
    if (this.validateProduct(this.updatedProduct) && this.editingProductId) {
      const productToSend: Partial<ProductDetailsDTO> = { ...this.updatedProduct };
      delete productToSend.trendingDisplayOrder;
      delete productToSend.productCategoryName; // Ensure productCategoryName is not sent to backend if it's a DTO prop
      delete productToSend.sellingDisplayOrder;

      this.productService.updateProduct(this.editingProductId, productToSend as ProductDetailsDTO, this.selectedEditFile).subscribe({
        next: () => {
          this.editingProductId = null;
          this.loadProducts();
          this.selectedEditFile = null;
          this.editImagePreviewUrl = null;
          this.snackBar.open('Product updated successfully!', 'Close', { duration: 2000, panelClass: ['success-snackbar'] });
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error updating product:', error);
          this.snackBar.open(`Failed to update product: ${error.error?.error || error.message}`, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
        }
      });
    }
  }

  cancelEdit(): void {
    this.editingProductId = null;
    this.selectedEditFile = null;
    this.editImagePreviewUrl = null;
  }

  deleteProduct(id: number): void {
    if (confirm('Are you sure you want to delete this product?')) {
      this.productService.deleteProduct(id).subscribe({
        next: () => {
          this.loadProducts();
          this.snackBar.open('Product deleted successfully!', 'Close', { duration: 2000, panelClass: ['success-snackbar'] });
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error deleting product:', error);
          this.snackBar.open(`Failed to delete product: ${error.error?.error || error.message}`, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
        }
      });
    }
  }

  cancelAdd(): void {
    this.showAddForm = false;
    this.resetNewProductForm();
  }

  private validateProduct(product: ProductDetailsDTO): boolean {
    if (!product.productName || product.productName.trim() === '') {
      this.snackBar.open('Product name is required', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      return false;
    }
    if (product.productQuantity === null || product.productQuantity === undefined || product.productQuantity <= 0) {
      this.snackBar.open('Quantity must be a positive number', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      return false;
    }
    if (product.price === null || product.price === undefined || product.price <= 0) {
      this.snackBar.open('Price must be a positive number', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      return false;
    }
    if (!product.productCategoryId) {
      this.snackBar.open('Category is required', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      return false;
    }
    return true;
  }

  private resetNewProductForm(): void {
    this.newProduct = {
      productName: '',
      productQuantity: 0,
      price: 0,
      productCategoryId: this.categories.length > 0 ? this.categories[0].productCategoryId! : 0,
      isActive: true,
      imageUrl: null,
      trendingDisplayOrder: null,
      sellingDisplayOrder: null
    };
    this.selectedNewFile = null;
    this.newImagePreviewUrl = null;
    const newFileInput = document.getElementById('newProductImage') as HTMLInputElement;
    if (newFileInput) {
      newFileInput.value = '';
    }
  }

  filterTrendingProducts(): void {
    const filterText = this.trendingProductFilter.toLowerCase();
    this.filteredProductsForTrending = this.products.filter(product =>
      product.productName.toLowerCase().includes(filterText) ||
      (product.productCategoryName && product.productCategoryName.toLowerCase().includes(filterText))
    ).sort((a, b) => {
        const aOrder = a.trendingDisplayOrder ?? Infinity;
        const bOrder = b.trendingDisplayOrder ?? Infinity;

        if (aOrder !== Infinity && bOrder !== Infinity) {
            return aOrder - bOrder;
        }
        if (aOrder !== Infinity) return -1;
        if (bOrder !== Infinity) return 1;
        return a.productName.localeCompare(b.productName);
    });
  }

  filterSellingProducts(): void {
    const filterText = this.sellingProductFilter.toLowerCase();
    this.filteredProductsForSelling = this.products.filter(product =>
      product.productName.toLowerCase().includes(filterText) ||
      (product.productCategoryName && product.productCategoryName.toLowerCase().includes(filterText))
    ).sort((a, b) => {
        const aOrder = a.sellingDisplayOrder ?? Infinity;
        const bOrder = b.sellingDisplayOrder ?? Infinity;

        if (aOrder !== Infinity && bOrder !== Infinity) {
            return aOrder - bOrder;
        }
        if (aOrder !== Infinity) return -1;
        if (bOrder !== Infinity) return 1;
        return a.productName.localeCompare(b.productName);
    });
  }

  onTrendingOrderChange(productId: number, newOrder: any): void {
    const orderValue = newOrder === '' || newOrder === null ? null : Number(newOrder);

    const product = this.products.find(p => p.productDetailsId === productId);
    if (product) {
      product.trendingDisplayOrder = (orderValue !== null && orderValue > 0) ? orderValue : null;
    }
    this.filterTrendingProducts();
  }

  onSellingOrderChange(productId: number, newOrder: any): void {
    const orderValue = newOrder === '' || newOrder === null ? null : Number(newOrder);

    const product = this.products.find(p => p.productDetailsId === productId);
    if (product) {
      product.sellingDisplayOrder = (orderValue !== null && orderValue > 0) ? orderValue : null;
    }
    this.filterSellingProducts();
  }

  saveTrendingOrders(): void {
    const updates: ProductOrderUpdateDTO[] = this.products
      .filter(p => p.trendingDisplayOrder !== undefined)
      .map(p => ({
        id: p.productDetailsId!,
        displayOrder: (p.trendingDisplayOrder !== null && p.trendingDisplayOrder !== undefined && p.trendingDisplayOrder > 0)
                      ? p.trendingDisplayOrder
                      : null
      }));

    if (updates.length === 0) {
      this.snackBar.open('No trending order changes to save.', 'Close', { duration: 2000 });
      return;
    }

    this.productService.updateTrendingProductOrders(updates).subscribe({
      next: () => {
        this.snackBar.open('Trending orders saved successfully!', 'Close', { duration: 2000, panelClass: ['success-snackbar'] });
        this.loadProducts();
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error saving trending orders:', err);
        this.snackBar.open(`Failed to save trending orders: ${err.error?.error || err.message}`, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      }
    });
  }

  saveSellingOrders(): void {
    const updates: ProductOrderUpdateDTO[] = this.products
      .filter(p => p.sellingDisplayOrder !== undefined)
      .map(p => ({
        id: p.productDetailsId!,
        displayOrder: (p.sellingDisplayOrder !== null && p.sellingDisplayOrder !== undefined && p.sellingDisplayOrder > 0)
                      ? p.sellingDisplayOrder
                      : null
      }));

    if (updates.length === 0) {
      this.snackBar.open('No selling order changes to save.', 'Close', { duration: 2000 });
      return;
    }

    this.productService.updateSellingProductOrders(updates).subscribe({
      next: () => {
        this.snackBar.open('Selling orders saved successfully!', 'Close', { duration: 2000, panelClass: ['success-snackbar'] });
        this.loadProducts();
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error saving selling orders:', err);
        this.snackBar.open(`Failed to save selling orders: ${err.error?.error || err.message}`, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      }
    });
  }

  getSortedTopTrendingProducts(): ProductDetailsDTO[] {
    return [...this.topTrendingProducts].sort((a, b) => (a.trendingDisplayOrder ?? Infinity) - (b.trendingDisplayOrder ?? Infinity));
  }

  getSortedTopSellingProducts(): ProductDetailsDTO[] {
    return [...this.topSellingProducts].sort((a, b) => (a.sellingDisplayOrder ?? Infinity) - (b.sellingDisplayOrder ?? Infinity));
  }

  // --- WISHLIST METHODS ---

  /**
   * Loads the user's wishlist entries from the backend.
   * Updates the `wishlistEntries` array and `userWishlistProductIds` set.
   */
  loadUserWishlist(): void {
    console.log('[ProductDetailsComponent] Loading user wishlist...');
    this.wishlistService.getWishlistByUserId(this.currentUserId).subscribe({
      next: (wishlistItems: WishlistDTO[]) => {
        this.wishlistEntries = wishlistItems; // Store the raw wishlist DTOs
        this.updateWishlistStatusForProducts(); // Update the set for quick lookup
        console.log('Wishlist loaded:', this.wishlistEntries);
        this.snackBar.open('Wishlist loaded successfully!', 'Close', { duration: 1500 });
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error loading wishlist:', err);
        this.snackBar.open(`Failed to load wishlist: ${err.error?.error || err.message}`, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      }
    });
  }

  /**
   * Adds a product to the user's wishlist.
   * @param productId The ID of the product to add.
   */
  addToWishlist(productId: number): void {
    console.log('[ProductDetailsComponent] Attempting to add product to wishlist:', productId);
    this.wishlistService.addToWishlist(this.currentUserId, productId).subscribe({
      next: (response) => {
        console.log('Product added to wishlist, response:', response);
        this.loadUserWishlist(); // Reload wishlist to update the list and status
        this.snackBar.open('Product added to wishlist!', 'Close', { duration: 2000, panelClass: ['success-snackbar'] });
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error adding to wishlist:', err);
        let errorMessage = 'Failed to add to wishlist.';
        if (err.status === 409) { // Conflict status from backend (already in wishlist)
          errorMessage = 'Product is already in your wishlist!';
        } else if (err.error && typeof err.error === 'string') {
          errorMessage = `Failed to add to wishlist: ${err.error}`;
        }
        this.snackBar.open(errorMessage, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      }
    });
  }

  /**
   * Removes a specific product from the wishlist using its unique wishlist ID.
   * @param wishlistId The unique ID of the wishlist entry to remove.
   */
  removeFromWishlist(wishlistId: number): void {
    console.log('[ProductDetailsComponent] Attempting to remove from wishlist:', wishlistId);
    this.wishlistService.removeFromWishlist(wishlistId).subscribe({
      next: () => {
        console.log('Product removed from wishlist.');
        this.loadUserWishlist(); // Reload wishlist to update the list and status
        this.snackBar.open('Product removed from wishlist!', 'Close', { duration: 2000, panelClass: ['success-snackbar'] });
      },
      error: (err: HttpErrorResponse) => {
        console.error('Error removing from wishlist:', err);
        this.snackBar.open(`Failed to remove from wishlist: ${err.error?.error || err.message}`, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      }
    });
  }

  /**
   * Checks if a product is currently in the user's wishlist.
   * @param productId The ID of the product to check.
   * @returns True if the product is in the wishlist, false otherwise.
   */
  isProductInWishlist(productId: number): boolean {
    return this.userWishlistProductIds.has(productId);
  }

  /**
   * Helper method to get the full ProductDetailsDTO for a given productId.
   * Used in the HTML to display product information in the wishlist section.
   * @param productId The ID of the product.
   * @returns The ProductDetailsDTO or undefined if not found.
   */
  getProductDetailsForWishlist(productId: number): ProductDetailsDTO | undefined {
    return this.products.find(p => p.productDetailsId === productId);
  }

  /**
   * Helper method to get the wishlistId for a given productId from the currently loaded wishlist.
   * This is used when removing an item directly from the main product list.
   * @param productId The ID of the product.
   * @returns The wishlistId if found, otherwise undefined.
   */
  getWishlistIdForProduct(productId: number): number | undefined {
    const wishlistEntry = this.wishlistEntries.find(entry => entry.productId === productId);
    return wishlistEntry ? wishlistEntry.wishlistId : undefined;
  }

  /**
   * Updates the set of product IDs that are in the user's wishlist.
   * This is called after loading the wishlist or performing an add/remove operation.
   */
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