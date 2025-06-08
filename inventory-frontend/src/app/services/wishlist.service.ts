import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { WishlistDTO } from '../models/wishlist-dto'; // Make sure this path is correct

@Injectable({
  providedIn: 'root'
})
export class WishlistService {
  // We need to explicitly add '/api' here because environment.apiUrl does not include it
  private apiUrl = `${environment.apiUrl}/api/wishlist`; // <-- **CHANGED THIS LINE**

  constructor(private http: HttpClient) { }

  /**
   * Adds a product to a user's wishlist.
   * Corresponds to backend: @PostMapping (with @RequestBody WishlistDTO)
   * @param userId The ID of the user.
   * @param productId The ID of the product to add.
   * @returns An Observable of the created WishlistDTO.
   */
  addToWishlist(userId: number, productId: number): Observable<WishlistDTO> {
    console.log(`[Frontend Service] Adding product ${productId} to wishlist for user ${userId}`);
    const wishlistEntry: WishlistDTO = { userId, productId }; // Create the DTO object
    return this.http.post<WishlistDTO>(this.apiUrl, wishlistEntry).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Retrieves all wishlist items for a specific user.
   * Corresponds to backend: @GetMapping("/user/{userId}")
   * @param userId The ID of the user.
   * @returns An Observable of an array of WishlistDTOs.
   */
  getWishlistByUserId(userId: number): Observable<WishlistDTO[]> {
    console.log(`[Frontend Service] Fetching wishlist for user ID: ${userId}`);
    return this.http.get<WishlistDTO[]>(`${this.apiUrl}/user/${userId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Removes a specific product from the wishlist using its unique wishlist ID.
   * Corresponds to backend: @DeleteMapping("/{wishlistId}")
   * @param wishlistId The unique ID of the wishlist entry to remove.
   * @returns An Observable that completes when the deletion is successful.
   */
  removeFromWishlist(wishlistId: number): Observable<void> {
    console.log(`[Frontend Service] Removing wishlist entry with ID: ${wishlistId}`);
    return this.http.delete<void>(`${this.apiUrl}/${wishlistId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Checks if a specific product is present in a user's wishlist.
   * Corresponds to backend: @GetMapping("/check/{userId}/{productId}")
   * @param userId The ID of the user.
   * @param productId The ID of the product to check.
   * @returns An Observable that emits true if the product is in the wishlist, false otherwise.
   */
  isProductInWishlist(userId: number, productId: number): Observable<boolean> {
    console.log(`[Frontend Service] Checking if product ${productId} is in wishlist for user ${userId}`);
    // Updated to use path variables as per backend controller
    return this.http.get<boolean>(`${this.apiUrl}/check/${userId}/${productId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get wishlistId for a specific product and user.
   * Corresponds to backend: @GetMapping("/id/{userId}/{productId}")
   * @param userId The ID of the user.
   * @param productId The ID of the product.
   * @returns An Observable of the wishlistId, or null if not found.
   */
  getWishlistIdForProduct(userId: number, productId: number): Observable<number | null> {
    console.log(`[Frontend Service] Getting wishlist ID for product ${productId} and user ${userId}`);
    return this.http.get<number>(`${this.apiUrl}/id/${userId}/${productId}`).pipe(
      catchError(error => {
        if (error.status === 404) {
          console.log(`[Frontend Service] Wishlist ID not found for user ${userId}, product ${productId}`);
          return throwError(() => null); // Return null in case of 404
        }
        return this.handleError(error); // Re-throw other errors
      })
    );
  }

  /**
   * Handles HTTP errors from the API calls.
   * @param error The HttpErrorResponse object.
   * @returns An Observable that emits an error.
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unknown error occurred!';
    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = `Client-side Error: ${error.error.message}`;
    } else {
      // Backend returned an unsuccessful response code.
      // Check for specific error message from the backend response body.
      if (error.error && error.error.message) {
        errorMessage = `Server Error (${error.status}): ${error.error.message}`;
      } else if (typeof error.error === 'string' && error.error.length > 0) {
          errorMessage = `Server Error (${error.status}): ${error.error}`;
      }
      else {
        errorMessage = `Server Error (${error.status}): ${error.statusText || 'Unknown Server Error'}`;
      }
    }
    console.error('[WishlistService ERROR]:', errorMessage, error);
    // Re-throw it as an Observable error for the component to handle
    return throwError(() => new Error(errorMessage));
  }
}