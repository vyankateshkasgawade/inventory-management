package com.application.entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date; 

@Entity
@Data 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseDetailsId;

    private Integer productCount;
    private Date purchaseDate; 
    private Boolean isActive;

    @Column(nullable = false) 
    private String productNameAtPurchase;

    @Column(nullable = false) 
    private String userNameAtPurchase;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "userId") 
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "productId") 
    private ProductDetails product;
}