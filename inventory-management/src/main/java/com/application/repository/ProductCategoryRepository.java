package com.application.repository;

import com.application.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
   // Optional<ProductCategory> findByProductCategoryName(String productCategoryName);
	Optional<ProductCategory> findByProductCategoryNameAndIsActiveTrue(String categoryName);

}
