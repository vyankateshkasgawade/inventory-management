export interface PurchaseDetails {
  purchaseDetailsId?: number;
  userId: number;
  userName?: string; // Added for display
  productId: number;
  productName?: string; // Added for display
  productCount: number;
  purchaseDate?: Date; // Added for display
  isActive?: boolean;
}