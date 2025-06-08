// src/app/dto/product-details-dto.ts

export interface ProductDetailsDTO {
  productDetailsId?: number;
  productName: string;
  productQuantity: number;
  price: number;
  productCategoryId: number;
  productCategoryName?: string; // Optional, populated on retrieve from backend
  isActive?: boolean;
  imageUrl?: string | null;

  // IMPORTANT: These fields MUST match your backend's database structure.
  // Your database now has 'trending_display_order' (integer) and 'selling_display_order' (integer).
  trendingDisplayOrder?: number | null; // Changed from isTopTrending: boolean
  sellingDisplayOrder?: number | null;  // Changed from isTopSelling: boolean
}

export class ProductDetails implements ProductDetailsDTO {
  constructor(
    public productName: string,
    public productQuantity: number,
    public price: number,
    public productCategoryId: number,
    public productDetailsId?: number,
    public productCategoryName?: string,
    public isActive?: boolean,
    public imageUrl?: string | null,
    // Constructor parameters must also match the DTO.
    public trendingDisplayOrder?: number | null, // Changed from isTopTrending: boolean
    public sellingDisplayOrder?: number | null   // Changed from isTopSelling: boolean
  ) {
    this.isActive = isActive !== undefined ? isActive : true;
    this.trendingDisplayOrder = trendingDisplayOrder !== undefined ? trendingDisplayOrder : null;
    this.sellingDisplayOrder = sellingDisplayOrder !== undefined ? sellingDisplayOrder : null;
  }
}