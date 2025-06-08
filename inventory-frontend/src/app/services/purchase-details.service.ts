import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PurchaseDetails } from '../models/purchase-details.model';

@Injectable({
  providedIn: 'root'
})
export class PurchaseDetailsService {
  private apiUrl = 'http://localhost:8080/api/purchases';

  constructor(private http: HttpClient) {}

  createPurchase(purchase: PurchaseDetails): Observable<PurchaseDetails> {
    return this.http.post<PurchaseDetails>(this.apiUrl, purchase);
  }

  getPurchaseById(id: number): Observable<PurchaseDetails> {
    return this.http.get<PurchaseDetails>(`${this.apiUrl}/${id}`);
  }

  getAllPurchases(): Observable<PurchaseDetails[]> {
    return this.http.get<PurchaseDetails[]>(this.apiUrl);
  }

  getPurchasesByUserId(userId: number): Observable<PurchaseDetails[]> {
    return this.http.get<PurchaseDetails[]>(`${this.apiUrl}/user/${userId}`);
  }

  updatePurchase(id: number, purchase: PurchaseDetails): Observable<PurchaseDetails> {
    return this.http.put<PurchaseDetails>(`${this.apiUrl}/${id}`, purchase);
  }

  deletePurchase(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
