import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProductCategory {
  productCategoryId?: number;
  productCategoryName: string;
  isActive?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ProductCategoryService {
  private baseUrl = 'http://localhost:8080/api/categories';

  constructor(private http: HttpClient) {}

  getAllCategories(): Observable<ProductCategory[]> {
    return this.http.get<ProductCategory[]>(this.baseUrl);
  }

  getCategoryById(id: number): Observable<ProductCategory> {
    return this.http.get<ProductCategory>(`${this.baseUrl}/${id}`);
  }

  createCategory(category: ProductCategory): Observable<ProductCategory> {
    return this.http.post<ProductCategory>(this.baseUrl, category);
  }

  updateCategory(id: number, category: ProductCategory): Observable<ProductCategory> {
    return this.http.put<ProductCategory>(`${this.baseUrl}/${id}`, category);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
