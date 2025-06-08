package com.application.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailsDTO {
    private Long productDetailsId;
    private String productName;
    private Integer productQuantity;
    private BigDecimal price;
    private Long productCategoryId;
    private String productCategoryName;
    private String imageUrl;
    private Boolean isActive;

    // These are the new fields for display order
    private Integer trendingDisplayOrder;
    private Integer sellingDisplayOrder;
}