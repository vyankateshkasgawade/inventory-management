// src/app/services/payment.service.ts

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment'; // Import environment for API URL

/**
 * Interface for the response received after creating a Razorpay order on the backend.
 * This ensures type safety for the data you get back from your create-order endpoint.
 */
export interface CreateOrderResponse {
  id: string; // Renamed from 'orderId' to 'id' to match backend 'order.get("id")'
  key: string;
  amount: number; // Amount from Razorpay API is typically a number (in paise), not string
  currency: string;
}

/**
 * Interface for the data sent to the backend for payment verification.
 * This provides type safety for the object containing Razorpay's payment details,
 * along with information about the purchase type (product vs. cart) and user ID.
 */
export interface VerifyPaymentRequest {
  order_id: string;
  payment_id: string;
  signature: string;
  userId: number; // User who is making the payment
  productId?: number; // Optional: for direct product purchase
  quantity?: number; // Optional: for direct product purchase
  isCartPurchase: boolean; // Flag: true if it's a cart purchase, false if direct product purchase
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private baseUrl = `${environment.apiUrl}/api/payment`; // Using environment.apiUrl for base URL

  constructor(private http: HttpClient) {}

  /**
   * Calls your Spring Boot backend to create a Razorpay order.
   * The amount should be sent in paise (e.g., ₹100 is 10000 paise).
   * @param amount The amount in paise.
   * @returns An Observable of type CreateOrderResponse.
   */
  createOrder(amount: number): Observable<CreateOrderResponse> {
    // The backend expects a simple object with an 'amount' property.
    return this.http.post<CreateOrderResponse>(`${this.baseUrl}/create-order`, { amount });
  }

  /**
   * Calls your Spring Boot backend to verify the Razorpay payment signature.
   * This is crucial for security to ensure the payment was not tampered with.
   * It now sends additional data like userId and purchase type.
   * @param data An object containing the Razorpay order_id, payment_id, signature, userId, and purchase type flags.
   * @returns An Observable<string>. The backend returns a success message string or an error.
   */
  verifyPayment(data: VerifyPaymentRequest): Observable<string> {
    return this.http.post(`${this.baseUrl}/verify`, data, { responseType: 'text' });
  }
}