export interface CartDTO {
  cartId?: number;
  userId: number;
  userName?: string; // This is a new addition - good for displaying user info if needed
  productId: number;
  productName?: string;
  quantity: number;
  addedDate?: Date;
  finalTotalAmount?: number;
  isActive?: boolean;
  price?: number; // Good, essential for client-side calculations
  imageUrl?: string | null; // Excellent, allows displaying images in the cart
}