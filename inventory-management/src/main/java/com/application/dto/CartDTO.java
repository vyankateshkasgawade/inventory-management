package com.application.dto;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDTO {
    private Long cartId;
    private Long userId;
    private String userName; // This will be set based on the user entity in the service
    private Long productId;
    private String productName; // This will be set based on the product entity in the service
    private Integer quantity;
    private Date addedDate;
    private BigDecimal finalTotalAmount;
    private Boolean isActive;
    public String imageUrl; // <--- ADD THIS LINE!
    private BigDecimal price;
    
}