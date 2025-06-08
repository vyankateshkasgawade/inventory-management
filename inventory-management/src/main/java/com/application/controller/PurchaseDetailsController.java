package com.application.controller;

import com.application.dto.PurchaseDetailsDTO;
import com.application.exception.ResourceNotFoundException;
import com.application.exception.PurchaseAlreadyExistsException;
import com.application.service.PurchaseDetailsService;
import com.application.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
//@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PurchaseDetailsController", description = "Operations related to Purchase Details management")
public class PurchaseDetailsController {

    private final PurchaseDetailsService purchaseDetailsService;
    
//===========================================create New Purchase=========================================================================================
    
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create a new purchase", description = "Create a new purchase for a user with specified product and details")
    @PostMapping
    public ResponseEntity<?> createNewPurchase(@RequestBody PurchaseDetailsDTO purchaseDTO) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Creating new purchase for user ID: {}, product ID: {}", 
                 purchaseDTO.getUserId(), purchaseDTO.getProductId());
        try {
            PurchaseDetailsDTO createdPurchase = purchaseDetailsService.createPurchase(purchaseDTO);
            return new ResponseEntity<>(createdPurchase, HttpStatus.CREATED);
        } catch (PurchaseAlreadyExistsException e) {
            log.warn("Purchase already exists for user ID: {}, product ID: {}", purchaseDTO.getUserId(), purchaseDTO.getProductId(), e);
            return handleException(e, HttpStatus.CONFLICT);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid input while creating purchase: {}", e.getMessage(), e);
            return handleException(e, HttpStatus.BAD_REQUEST);
        } catch (DataIntegrityViolationException e) {
            log.warn("Database constraint violated during purchase creation: {}", e.getMessage(), e);
            return handleException(e, HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            log.error(AppConstants.CONTROLLER_LOG_PREFIX + "Error creating purchase: {}", purchaseDTO, e);
            return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } 
    }
//=============================================get Purchase Details By Purchase DetailsId================================================
    
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get purchase details by ID", description = "Fetch the details of a purchase by its unique ID")
    @GetMapping("/{purchaseDetailsId}")
    public ResponseEntity<?> getPurchaseDetailsByPurchaseDetailsId(@PathVariable Long purchaseDetailsId) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Fetching purchase by ID: {}", purchaseDetailsId);
        try {
            PurchaseDetailsDTO purchase = purchaseDetailsService.getPurchaseByPurchaseDetailsId(purchaseDetailsId);
            return new ResponseEntity<>(purchase, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid purchase ID: {}", e.getMessage(), e);
            return handleException(e, HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            return handleException(e, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(AppConstants.CONTROLLER_LOG_PREFIX + "Error fetching purchase by ID: {}", purchaseDetailsId, e);
            return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //===================================get All Purchase Details ===============================================
    
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all purchase details", description = "Fetch all the purchase details available in the system")
    @GetMapping
    public ResponseEntity<?> getAllPurchaseDetails() {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Fetching all purchases");
        try {
            List<PurchaseDetailsDTO> purchases = purchaseDetailsService.getAllPurchases();
            return new ResponseEntity<>(purchases, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Runtime error while fetching purchases: {}", e.getMessage(), e);
            return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } 
    }
    
//======================================get All Purchase Details By UserId=============================================
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get all purchases by user ID", description = "Fetch all purchases associated with a specific user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllPurchaseDetailsByUserId(@PathVariable Long userId) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Fetching purchases by user ID: {}", userId);
        try {
            List<PurchaseDetailsDTO> purchases = purchaseDetailsService.getPurchasesByUserId(userId);
            return new ResponseEntity<>(purchases, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid user ID while fetching purchases: {}", e.getMessage(), e);
            return handleException(e, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error fetching purchases by user ID {}: {}", userId, e);
            return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
//==========================================update Purchase By Purchase DetailsId===================================================
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Update purchase details", description = "Update an existing purchase by its unique ID")
    @PutMapping("/{purchaseDetailsId}")
    public ResponseEntity<?> updatePurchaseByPurchaseDetailsId(@PathVariable Long purchaseDetailsId, @RequestBody PurchaseDetailsDTO purchaseDTO) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Updating purchase by ID: {}", purchaseDetailsId);
        try {
            PurchaseDetailsDTO updatedPurchase = purchaseDetailsService.updatePurchaseBypurchaseDetailsId(purchaseDetailsId, purchaseDTO);
            return new ResponseEntity<>(updatedPurchase, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid update input: {}", e.getMessage(), e);
            return handleException(e, HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            return handleException(e, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(AppConstants.CONTROLLER_LOG_PREFIX + "Error updating purchase by ID: {}", purchaseDetailsId, e);
            return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //======================================delete Purchase Details By Purchase DetailsId===============================================
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete purchase by ID", description = "Delete an existing purchase by its unique ID")
    @DeleteMapping("/{purchaseDetailsId}")
    public ResponseEntity<?> deletePurchaseDetailsByPurchaseDetailsId(@PathVariable Long purchaseDetailsId) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Deleting purchase by ID: {}", purchaseDetailsId);
        try {
            purchaseDetailsService.deletePurchaseByPurchaseDetailsId(purchaseDetailsId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid delete request: {}", e.getMessage(), e);
            return handleException(e, HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            return handleException(e, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(AppConstants.CONTROLLER_LOG_PREFIX + "Error deleting purchase by ID: {}", purchaseDetailsId, e);
            return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<String> handleException(Exception e, HttpStatus status) {
        return new ResponseEntity<>(e.getMessage(), status);
    }
}
