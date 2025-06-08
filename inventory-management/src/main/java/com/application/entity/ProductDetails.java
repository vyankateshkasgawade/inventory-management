package com.application.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productDetailsId;

    @Column(nullable = false, unique = true)
    private String productName;

    private Integer productQuantity;

    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_category_id", nullable = false)
    private ProductCategory productCategory;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "image_url")
    private String imageUrl;

    // Change from Boolean to Integer for display order
    // Make them nullable in the DB as a product might not be in a specific list
    @Column(name = "trending_display_order")
    private Integer trendingDisplayOrder;

    @Column(name = "selling_display_order")
    private Integer sellingDisplayOrder;

    // Get product category name for DTO mapping
    public String getProductCategoryName() {
        return this.productCategory != null ? this.productCategory.getProductCategoryName() : null;
    }
}