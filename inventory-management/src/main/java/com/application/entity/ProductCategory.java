package com.application.entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productCategoryId;

    private String productCategoryName;
    private Boolean isActive;

    @OneToMany(mappedBy = "productCategory", cascade = CascadeType.ALL)
    private List<ProductDetails> productDetails;
}

