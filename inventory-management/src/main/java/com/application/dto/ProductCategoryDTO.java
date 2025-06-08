package com.application.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategoryDTO {
    private Long productCategoryId;
    private String productCategoryName;
    private Boolean isActive;
}
