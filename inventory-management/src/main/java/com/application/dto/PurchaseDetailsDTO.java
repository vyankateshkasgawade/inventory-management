package com.application.dto;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseDetailsDTO {
    private Long purchaseDetailsId;
    private Long userId;
    private String userName;
    private Long productId;
    private String productName;
    private Integer productCount;
    private Date purchaseDate;
    private Boolean isActive;
}
