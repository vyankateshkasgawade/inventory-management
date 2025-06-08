

package com.application.entity;

import jakarta.persistence.*; 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "carts") 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId; // Use Long for ID to match repository

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user; // Assuming your User entity is named AppUser

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductDetails product;

    // <<< ADD THESE TWO FIELDS >>>
    @Column(name = "product_name") // Optional: Specify column name if different
    private String productName;

    @Column(name = "image_url") // Optional: Specify column name if different
    private String imageUrl;
    // <<< END OF ADDITION >>>

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "added_date")
    @Temporal(TemporalType.TIMESTAMP) // Or TemporalType.DATE if you only need date
    private Date addedDate;

    @Column(name = "final_total_amount", precision = 19, scale = 2) // Precision and scale for BigDecimal
    private BigDecimal finalTotalAmount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

}