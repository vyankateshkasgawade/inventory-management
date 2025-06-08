package com.application.repository;
import com.application.entity.PurchaseDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PurchaseDetailsRepository extends JpaRepository<PurchaseDetails, Long> {

    List<PurchaseDetails> findByUser_UserId(Long userId);

    @Query("SELECT p FROM PurchaseDetails p WHERE p.user.userId = :userId AND p.product.productDetailsId = :productId AND DATE(p.purchaseDate) = DATE(:purchaseDate)")
    Optional<PurchaseDetails> findByUser_UserIdAndProduct_ProductDetailsIdAndPurchaseDate(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("purchaseDate") Date purchaseDate
    );
    
    
    Optional<PurchaseDetails> findByUser_UserIdAndProduct_ProductDetailsIdAndPurchaseDateBetween
    (
    	    Long userId, Long productId, Date startDate, Date endDate);

}