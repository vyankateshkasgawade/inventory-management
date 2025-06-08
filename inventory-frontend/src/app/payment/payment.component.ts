import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatRadioModule } from '@angular/material/radio';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';

// DTOs and Services
// >>> CHANGE THIS LINE: Import ProductDetailsDTO directly from its DTO file <<<
import { ProductDetailsDTO } from '../dto/product-details-dto'; // Assuming this is the correct path
import { ProductDetailsService } from '../services/product-details.service'; // Keep importing the service
import { PaymentService, CreateOrderResponse, VerifyPaymentRequest } from '../services/payment.service';
import { CartDTO } from '../models/cart.model';
import { CartService } from '../services/cart.service';
import { AuthService } from '../auth.service';
import { environment } from '../../environments/environment';

// Declare Razorpay globally as it's loaded via a script
declare const Razorpay: any;

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatDividerModule,
    MatRadioModule,
    MatInputModule,
    MatSelectModule,
    MatFormFieldModule,
    FormsModule
  ],
  templateUrl: './payment.component.html',
  styleUrl: './payment.component.css'
})
export class PaymentComponent implements OnInit {
  product: ProductDetailsDTO | null = null;
  cartItems: CartDTO[] = [];
  totalCartAmount: number = 0;
  selectedPaymentOption: string | null = null;
  paymentType: 'product' | 'cart' = 'product';
  userId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private productDetailsService: ProductDetailsService,
    private cartService: CartService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    console.log('PaymentComponent ngOnInit called.');

    const userIdFromQueryParams = Number(this.route.snapshot.queryParamMap.get('userId'));
    if (!isNaN(userIdFromQueryParams) && userIdFromQueryParams > 0) {
      this.userId = userIdFromQueryParams;
      console.log(`[PaymentComponent Debug] Attempt 1: userId from queryParams: ${userIdFromQueryParams}`);
    } else {
      console.log(`[PaymentComponent Debug] Attempt 1: userId from queryParams: null or invalid (${userIdFromQueryParams})`);
    }

    if (this.userId === null) {
      const userIdFromAuthService = this.authService.getCurrentUserId();
      this.userId = userIdFromAuthService;
      console.log(`[PaymentComponent Debug] Attempt 2: userId from AuthService: ${userIdFromAuthService}`);
    }

    console.log(`[PaymentComponent Debug] Final userId after all attempts: ${this.userId}`);

    if (this.userId === null) {
      console.error('[PaymentComponent] User not logged in. Redirecting to login.');
      alert('You must be logged in to make a purchase.');
      this.router.navigate(['/login']);
      return;
    }

    this.route.paramMap.subscribe(params => {
      const type = params.get('type');
      const idParam = params.get('id');

      if (type === 'cart') {
        this.paymentType = 'cart';
        console.log('[PaymentComponent] Processing cart payment for user:', this.userId);
        this.fetchCartDetails(this.userId!);
      } else if (type === 'product' && idParam) {
        this.paymentType = 'product';
        const productId = Number(idParam);
        if (!isNaN(productId) && productId > 0) {
          console.log('[PaymentComponent] Processing direct product payment for product ID:', productId);
          this.fetchProductDetails(productId);
        } else {
          console.warn('[PaymentComponent] Invalid product ID in route for product payment. Redirecting to dashboard.');
          alert('Invalid product ID for direct payment. Redirecting to dashboard.');
          this.router.navigate(['/dashboard']);
        }
      } else {
        console.warn('[PaymentComponent] Invalid payment route or missing parameters. Redirecting to dashboard.');
        alert('Invalid payment request. Redirecting to dashboard.');
        this.router.navigate(['/dashboard']);
      }
    });
  }

  private fetchProductDetails(productId: number): void {
    this.productDetailsService.getProductByProductId(productId).subscribe(
      (data: ProductDetailsDTO) => {
        this.product = data;
        console.log('[PaymentComponent] Product data fetched successfully:', this.product);
        if (this.product && (typeof this.product.price !== 'number' || this.product.price <= 0)) {
          console.error('[PaymentComponent] Product price is invalid or missing:', this.product?.price);
          alert('Invalid product price. Cannot proceed with payment.');
          this.router.navigate(['/dashboard']);
        }
      },
      (error: any) => {
        console.error('[PaymentComponent] Error fetching product details:', error);
        alert('Failed to load product details. Redirecting to dashboard.');
        this.router.navigate(['/dashboard']);
      }
    );
  }

  private fetchCartDetails(userId: number): void {
    this.cartService.getCartByUserId(userId).subscribe(
      (data: CartDTO[]) => {
        this.cartItems = data.filter(item => item.isActive);
        if (this.cartItems.length === 0) {
          console.warn('[PaymentComponent] No active cart items found for user:', userId);
          alert('Your cart is empty. Nothing to pay for.');
          this.router.navigate(['/cart']);
          return;
        }
        this.totalCartAmount = this.cartItems.reduce((sum, item) => sum + (item.finalTotalAmount || 0), 0);
        console.log('[PaymentComponent] Cart items fetched successfully:', this.cartItems);
        console.log('[PaymentComponent] Total cart amount:', this.totalCartAmount);
        if (this.totalCartAmount <= 0) {
          console.error('[PaymentComponent] Total cart amount is invalid or zero:', this.totalCartAmount);
          alert('Invalid cart amount. Cannot proceed with payment.');
          this.router.navigate(['/cart']);
        }
      },
      (error) => {
        console.error('[PaymentComponent] Error fetching cart details:', error);
        alert('Failed to load cart details. Redirecting to cart.');
        this.router.navigate(['/cart']);
      }
    );
  }

  private loadRazorpayScript(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (typeof Razorpay !== 'undefined') {
        console.log('Razorpay script already loaded.');
        resolve();
        return;
      }

      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.onload = () => {
        console.log('Razorpay script loaded successfully.');
        resolve();
      };
      script.onerror = (error: Event | string) => {
        const errorMessage = typeof error === 'string' ? error : (error as Event).type;
        console.error(`Failed to load Razorpay script: ${errorMessage}`, error);
        reject(new Error(`Failed to load Razorpay script: ${errorMessage}`));
      };
      document.body.appendChild(script);
    });
  }

  async payNow(): Promise<void> {
    console.log('--- payNow() called ---');
    console.log('Selected Payment Option:', this.selectedPaymentOption);
    console.log('Payment Type:', this.paymentType);
    console.log('User ID:', this.userId);

    let amountToPay: number;
    let descriptionText: string;

    if (this.paymentType === 'product') {
      if (!this.product || typeof this.product.price !== 'number' || this.product.price <= 0) {
        console.error('Cannot proceed: No product data or invalid price.');
        alert('Error: Product details or price missing/invalid. Please try again.');
        return;
      }
      amountToPay = this.product.price;
      descriptionText = this.product.productName || 'Direct Product Purchase';
    } else {
      if (this.cartItems.length === 0 || this.totalCartAmount <= 0) {
        console.error('Cannot proceed: No active cart items or invalid total amount.');
        alert('Error: Cart is empty or total amount is invalid. Please add items to cart.');
        return;
      }
      amountToPay = this.totalCartAmount;
      descriptionText = 'Cart Purchase';
    }

    if (!this.selectedPaymentOption) {
      console.error('Cannot proceed: No payment option selected.');
      alert('Please select a payment method to proceed.');
      return;
    }

    if (this.userId === null) {
      console.error('Cannot proceed: User ID is missing (runtime check in payNow).');
      alert('User information missing. Please log in again.');
      this.router.navigate(['/login']);
      return;
    }

    try {
      await this.loadRazorpayScript();
    } catch (error) {
      console.error(error);
      alert('Error loading payment gateway. Please refresh the page.');
      return;
    }

    const amountInPaise = Math.round(amountToPay * 100);
    console.log(`Attempting to create order for ${amountToPay} (INR ${amountInPaise} paise).`);

    this.paymentService.createOrder(amountInPaise).subscribe({
      next: (res: CreateOrderResponse) => {
        console.log('[PaymentComponent] Razorpay order created successfully by backend:', res);

        if (!res.key || !res.id || !res.amount || !res.currency) {
          console.error('Error: Incomplete Razorpay order details received from backend.', res);
          alert('Payment gateway configuration error. Please try again later. (Missing Order ID or Key)');
          return;
        }

        const options = {
          key: res.key,
          amount: res.amount,
          currency: res.currency,
          name: 'E-Shop',
          description: descriptionText,
          order_id: res.id,
          handler: (response: any) => {
            console.log('Razorpay payment successful callback received:', response);

            const verificationData: VerifyPaymentRequest = {
              order_id: res.id,
              payment_id: response.razorpay_payment_id,
              signature: response.razorpay_signature,
              userId: this.userId!,
              isCartPurchase: false
            };

            if (this.paymentType === 'product' && this.product) {
              verificationData.productId = this.product.productDetailsId;
              verificationData.quantity = 1;
              verificationData.isCartPurchase = false;
            } else if (this.paymentType === 'cart') {
              verificationData.isCartPurchase = true;
            }

            this.paymentService.verifyPayment(verificationData).subscribe({
              next: () => {
                alert('✅ Payment Successful! Your purchase has been recorded.');
                console.log('Payment successfully verified by backend.');
                if (this.paymentType === 'cart' && this.userId) {
                  this.cartService.clearCartByUserId(this.userId).subscribe({
                    next: () => console.log('Cart cleared successfully after payment.'),
                    error: (cartClearErr) => console.error('Failed to clear cart after payment:', cartClearErr)
                  });
                }
                this.router.navigate(['/dashboard']);
              },
              error: (err) => {
                console.error('Payment verification failed on backend:', err);
                alert('❌ Payment Verification Failed! Please contact support if amount was deducted.');
                this.router.navigate(['/dashboard']);
              }
            });
          },
          prefill: {
            name: 'Vyankatesh',
            email: 'vyankatesh@example.com',
            contact: '9999999999'
          },
          theme: {
            color: '#0f9d58'
          }
        };

        const rzp = new Razorpay(options);
        rzp.on('payment.failed', (response: any) => {
          console.error("Razorpay Payment failed:", response.error.code, response.error.description, response.error.source, response.error.step, response.error.reason, response.error.metadata);
          alert(`Payment Failed: ${response.error.description || 'Unknown error'}. Please try again.`);
        });

        rzp.open();
      },
      error: (err) => {
        console.error('Error creating Razorpay order on frontend side (backend communication error):', err);
        alert('❌ Failed to initiate payment. Please try again later.');
      }
    });
  }
}