package com.application.service;

import com.application.dto.PurchaseDetailsDTO;
import java.util.List;

public interface PurchaseDetailsService {
    PurchaseDetailsDTO createPurchase(PurchaseDetailsDTO purchaseDTO);

    // ADD THIS METHOD SIGNATURE
    List<PurchaseDetailsDTO> createPurchasesFromCart(Long userId);

    PurchaseDetailsDTO getPurchaseByPurchaseDetailsId(Long purchaseDetailsId);

    List<PurchaseDetailsDTO> getAllPurchases();

    List<PurchaseDetailsDTO> getPurchasesByUserId(Long userId);

    PurchaseDetailsDTO updatePurchaseBypurchaseDetailsId(Long purchaseDetailsId, PurchaseDetailsDTO purchaseDTO);

    void deletePurchaseByPurchaseDetailsId(Long purchaseDetailsId);
}