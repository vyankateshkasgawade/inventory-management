// src/app/services/cart.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CartDTO } from '../models/cart.model'; // Correct import for CartDTO
import { ProductDetailsDTO } from '../dto/product-details-dto'; // Correct import for ProductDetailsDTO

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private baseUrl = `${environment.apiUrl}/api/cart`; // Using environment.apiUrl for base URL
  private productApiUrl = `${environment.apiUrl}/api/products`; // Using environment.apiUrl for product API

  constructor(private http: HttpClient) {}

  /**
   * Adds an item to the user's cart.
   * @param cart The CartDTO object containing userId, productId, and quantity.
   * @returns An Observable of the added CartDTO.
   */
  addItemToCart(cart: CartDTO): Observable<CartDTO> {
    return this.http.post<CartDTO>(this.baseUrl, cart);
  }

  /**
   * Fetches all active products from the product service.
   * Used for populating the product dropdown in the cart component (if applicable).
   * @returns An Observable of an array of ProductDetailsDTO.
   */
  getAllProducts(): Observable<ProductDetailsDTO[]> {
    return this.http.get<ProductDetailsDTO[]>(this.productApiUrl);
  }

  /**
   * Fetches a single cart item by its cartId.
   * @param cartId The ID of the cart item.
   * @returns An Observable of a single CartDTO.
   */
  getCartItemById(cartId: number): Observable<CartDTO> {
    return this.http.get<CartDTO>(`${this.baseUrl}/${cartId}`);
  }

  /**
   * Fetches all active cart items for a specific user.
   * @param userId The ID of the user.
   * @returns An Observable of an array of CartDTO.
   */
  getCartByUserId(userId: number): Observable<CartDTO[]> {
    return this.http.get<CartDTO[]>(`${this.baseUrl}/user/${userId}`);
  }

  /**
   * Updates an existing cart item.
   * @param cartId The ID of the cart item to update.
   * @param cart The updated CartDTO object.
   * @returns An Observable of the updated CartDTO.
   */
  updateCart(cartId: number, cart: CartDTO): Observable<CartDTO> {
    return this.http.put<CartDTO>(`${this.baseUrl}/${cartId}`, cart);
  }

  /**
   * Deletes (soft deletes) a cart item by its cartId.
   * @param cartId The ID of the cart item to delete.
   * @returns An Observable that completes when the operation is successful.
   */
  deleteCartItem(cartId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${cartId}`);
  }

  /**
   * Clears all active cart items for a specific user after a successful purchase.
   * This typically involves marking them as inactive on the backend.
   * @param userId The ID of the user whose cart should be cleared.
   * @returns An Observable that completes when the operation is successful.
   */
  clearCartByUserId(userId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/clear/${userId}`);
  }
}