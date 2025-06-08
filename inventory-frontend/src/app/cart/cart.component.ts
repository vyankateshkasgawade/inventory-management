import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

import { CartDTO } from '../models/cart.model';
import { ProductDetailsDTO } from '../dto/product-details-dto';
import { CartService } from '../services/cart.service';
import { AuthService } from '../auth.service';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    FormsModule,
    MatTableModule,
    MatIconModule,
    MatTooltipModule
  ],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css'],
})
export class CartComponent implements OnInit {
  cartItems: CartDTO[] = [];
  products: ProductDetailsDTO[] = [];
  newCart: CartDTO = {
    userId: 0,
    productId: 0,
    quantity: 1
  };

  userId: number = 0;
  selectedCartItem?: CartDTO;
  selectedCartId: number = 0;

  displayedColumns: string[] = ['image', 'product', 'quantity', 'price', 'total', 'actions'];
  backendBaseUrl = environment.apiUrl;

  constructor(
    private router: Router,
    private cartService: CartService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    console.log('CartComponent ngOnInit called: Attempting token retrieval for userId');

    const authenticatedUserId = this.authService.getCurrentUserId();

    if (authenticatedUserId) {
      this.userId = authenticatedUserId;
      console.log('Authenticated userId:', this.userId);
      this.loadCartItems();
      this.loadAllProducts();
      this.resetNewCart();
    } else {
      console.warn('User not authenticated. Redirecting to login.');
      alert('You must be logged in to view your cart.');
      this.router.navigate(['/login']);
    }
  }

  addToCart(): void {
    this.newCart.userId = this.userId;

    const selectedProduct = this.products.find(p => p.productDetailsId === this.newCart.productId);

    if (!selectedProduct) {
      alert('Please select a valid product to add to cart.');
      return;
    }

    this.newCart.productName = selectedProduct.productName;
    this.newCart.price = selectedProduct.price;
    this.newCart.imageUrl = selectedProduct.imageUrl;
    this.newCart.finalTotalAmount = (this.newCart.quantity || 0) * (selectedProduct.price || 0);

    this.cartService.addItemToCart(this.newCart).subscribe({
      next: (addedItem) => {
        console.log('Product added to cart:', addedItem);
        this.loadCartItems();
        this.resetNewCart();
        alert('Item added to cart successfully!');
      },
      error: (err) => {
        console.error('Error adding to cart', err);
        alert(err.error?.message || err.error || 'Failed to add item to cart.');
      }
    });
  }

  loadCartItems(): void {
    if (this.userId === 0) {
      console.warn('User ID not set, cannot load cart items.');
      return;
    }

    this.cartService.getCartByUserId(this.userId).subscribe({
      next: (data) => {
        console.log('Raw cart items from API:', data);
        this.cartItems = data.filter(item => item.isActive !== false);
      },
      error: (err) => {
        console.error('Error loading cart items', err);
        alert(err.error?.message || err.error || 'Failed to load cart items.');
      }
    });
  }

  loadAllProducts(): void {
    this.cartService.getAllProducts().subscribe({
      next: (data) => {
        this.products = data.filter(p => p.isActive !== false);
      },
      error: (err) => {
        console.error('Error loading products for dropdown', err);
        alert('Failed to load product list for selection.');
      }
    });
  }

  getCartItemById(): void {
    if (!this.selectedCartId) {
      alert('Please enter a cart ID');
      return;
    }

    this.cartService.getCartItemById(this.selectedCartId).subscribe({
      next: (data) => {
        this.selectedCartItem = data;
        console.log('Fetched cart item by ID:', data);
        alert('Cart item fetched. Check console for details.');
      },
      error: (err) => {
        console.error('Error fetching cart item by ID', err);
        alert(err.error?.message || err.error || 'Failed to fetch cart item.');
      }
    });
  }

  updateCart(cart: CartDTO): void {
    if (cart.cartId) {
      this.cartService.updateCart(cart.cartId, cart).subscribe({
        next: (updatedItem) => {
          console.log('Cart item updated:', updatedItem);
          this.loadCartItems();
        },
        error: (err) => {
          console.error('Error updating cart quantity', err);
          alert(err.error?.message || err.error || 'Failed to update cart item quantity.');
          this.loadCartItems();
        }
      });
    }
  }

  deleteCart(cartId: number): void {
    if (!cartId) {
      alert('Cannot delete: Cart ID is missing.');
      return;
    }
    if (confirm('Are you sure you want to remove this item from your cart?')) {
      this.cartService.deleteCartItem(cartId).subscribe({
        next: () => {
          console.log('Cart item deleted (soft delete):', cartId);
          this.loadCartItems();
          alert('Item removed from cart!');
        },
        error: (err) => {
          console.error('Error deleting cart item', err);
          alert(err.error?.message || err.error || 'Failed to remove item from cart.');
        }
      });
    }
  }

  incrementQuantity(item: CartDTO): void {
    item.quantity = (item.quantity || 0) + 1;
    this.updateCartAndRecalculate(item);
  }

  decrementQuantity(item: CartDTO): void {
    if ((item.quantity || 0) > 1) {
      item.quantity = (item.quantity || 0) - 1;
      this.updateCartAndRecalculate(item);
    } else {
      this.deleteCart(item.cartId!);
    }
  }

  onQuantityChange(item: CartDTO): void {
    if (item.quantity === null || item.quantity === undefined || item.quantity < 1) {
      item.quantity = 1;
    }
    this.updateCartAndRecalculate(item);
  }

  updateCartAndRecalculate(item: CartDTO): void {
    item.finalTotalAmount = (item.quantity || 0) * (item.price || 0);
    this.updateCart(item);
  }

  getProductName(productId: number | undefined): string {
    if (productId === undefined) return 'N/A';
    const product = this.products.find(p => p.productDetailsId === productId);
    return product ? product.productName || 'N/A' : 'N/A';
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

  getCartTotal(): number {
    return this.cartItems.reduce((sum, item) => sum + (item.finalTotalAmount ?? 0), 0);
  }

  private resetNewCart(): void {
    this.newCart = {
      userId: this.userId,
      productId: 0,
      quantity: 1,
      productName: undefined,
      price: undefined,
      imageUrl: undefined
    };
  }

  // ====================================================================================================
  // MODIFIED PAYMENT INTEGRATION LOGIC - NOW USING QUERY PARAMS
  // This method now ONLY navigates to the PaymentComponent.
  // All Razorpay initiation logic has been removed from here.
  // ====================================================================================================
  proceedToCheckout(): void {
    console.log('--- proceedToCheckout() called from Cart ---');

    const totalAmount = this.getCartTotal();

    if (totalAmount <= 0 || this.cartItems.length === 0) {
      alert('Your cart is empty or total amount is zero. Cannot proceed to checkout.');
      return;
    }

    if (!this.userId) {
      console.error('User ID is missing in CartComponent. Cannot proceed with payment.');
      alert('User information missing. Please log in again.');
      this.router.navigate(['/login']);
      return;
    }

    // THIS IS THE ONLY ACTION HERE: Navigate to the PaymentComponent
    // Pass 'cart' as the type and '0' as a dummy ID, and crucially, pass the actual userId as a query parameter.
    this.router.navigate(
      ['/payment', 'cart', 0], // '0' is a placeholder for 'id' param when type is 'cart'
      { queryParams: { userId: this.userId } } // <--- IMPORTANT: Pass userId as a query parameter
    );

    console.log(`CartComponent navigating to /payment/cart with userId in queryParams: ${this.userId}`);
  }

  // This method remains here, but typically it would be called by the PaymentComponent
  // after successful payment verification.
  clearUserCart(): void {
    if (this.userId === 0) {
      console.warn('User ID not set, cannot clear cart.');
      return;
    }
    this.cartService.clearCartByUserId(this.userId).subscribe({
      next: () => {
        console.log(`Cart for user ${this.userId} cleared successfully.`);
        this.cartItems = [];
      },
      error: (err) => {
        console.error('Error clearing cart:', err);
      }
    });
  }
}