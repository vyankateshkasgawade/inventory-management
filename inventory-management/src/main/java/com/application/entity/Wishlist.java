package com.application.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wishlist_entries") 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wishlistId; // Unique ID for the wishlist entry

    @Column(name = "user_id", nullable = false)
    private Long userId; // The ID of the user who owns this wishlist entry

    @Column(name = "product_id", nullable = false)
    private Long productId; // The ID of the product in the wishlist

    // You might want to add a timestamp
    // @Column(name = "added_date")
    // private LocalDateTime addedDate;

    // Optional: If you have User and Product entities, you can add relationships
    // @ManyToOne
    // @JoinColumn(name = "user_id", insertable = false, updatable = false)
    // private User user; // Assuming you have a User entity

    // @ManyToOne
    // @JoinColumn(name = "product_id", insertable = false, updatable = false)
    // private Product product; // Assuming you have a Product entity
}
