// src/app/services/product-details.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
// IMPORTANT: Import ProductDetailsDTO from its single source of truth
import { ProductDetailsDTO } from '../dto/product-details-dto'; // Correct path to your DTO file
import { environment } from '../../environments/environment';


// This interface is specific to the update operations for display orders
export interface ProductOrderUpdateDTO {
  id: number;
  displayOrder: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class ProductDetailsService {
  private apiUrl = `${environment.apiUrl}/api/products`;

  constructor(private http: HttpClient) {}

  getAllProducts(): Observable<ProductDetailsDTO[]> {
    return this.http.get<ProductDetailsDTO[]>(this.apiUrl);
  }

  // CORRECTED: Renamed to getProductByProductId
  getProductByProductId(id: number): Observable<ProductDetailsDTO> {
    return this.http.get<ProductDetailsDTO>(`${this.apiUrl}/${id}`);
  }

  getTopTrendingProducts(): Observable<ProductDetailsDTO[]> {
    return this.http.get<ProductDetailsDTO[]>(`${this.apiUrl}/trending`);
  }

  getTopSellingProducts(): Observable<ProductDetailsDTO[]> {
    return this.http.get<ProductDetailsDTO[]>(`${this.apiUrl}/selling`);
  }

  getProductsByCategory(categoryId: number): Observable<ProductDetailsDTO[]> {
    return this.http.get<ProductDetailsDTO[]>(`${this.apiUrl}/category/${categoryId}`);
  }

  createProduct(product: ProductDetailsDTO, imageFile: File | null): Observable<ProductDetailsDTO> {
    const formData = this.buildFormData(product, imageFile);
    return this.http.post<ProductDetailsDTO>(this.apiUrl, formData);
  }

  updateProduct(id: number, product: ProductDetailsDTO, imageFile: File | null): Observable<ProductDetailsDTO> {
    const formData = this.buildFormData(product, imageFile);
    return this.http.put<ProductDetailsDTO>(`${this.apiUrl}/${id}`, formData);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  updateTrendingProductOrders(updates: ProductOrderUpdateDTO[]): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/update-trending-order`, updates);
  }

  updateSellingProductOrders(updates: ProductOrderUpdateDTO[]): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/update-selling-order`, updates);
  }

  private buildFormData(product: ProductDetailsDTO, imageFile: File | null): FormData {
    const formData = new FormData();

    const productDataForBackend: Partial<ProductDetailsDTO> = {
      productDetailsId: product.productDetailsId,
      productName: product.productName,
      productQuantity: product.productQuantity,
      price: product.price,
      productCategoryId: product.productCategoryId,
      imageUrl: product.imageUrl,
      isActive: product.isActive
    };

    formData.append(
      'product',
      new Blob([JSON.stringify(productDataForBackend)], { type: 'application/json' })
    );

    if (imageFile) {
      formData.append('image', imageFile, imageFile.name);
    }
    return formData;
  }
}
// REMOVED: export { ProductDetailsDTO }; <-- This line should be gone