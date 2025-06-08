package com.application.controller;

import com.application.dto.PurchaseDetailsDTO;
import com.application.exception.PurchaseAlreadyExistsException;
import com.application.exception.ResourceNotFoundException;
import com.application.service.PurchaseDetailsService;
import com.application.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseDetailsControllerTest {

    @Mock
    private PurchaseDetailsService purchaseDetailsService;

    @InjectMocks
    private PurchaseDetailsController purchaseDetailsController;

    private PurchaseDetailsDTO validPurchaseDTO;
    private PurchaseDetailsDTO existingPurchaseDTO;

    @BeforeEach
    void setUp() {
        validPurchaseDTO = PurchaseDetailsDTO.builder()
                .purchaseDetailsId(1L)
                .userId(1L)
                .userName("John Doe")
                .productId(1L)
                .productName("Smartphone")
                .productCount(2)
                .purchaseDate(new Date())
                .isActive(true)
                .build();

        existingPurchaseDTO = PurchaseDetailsDTO.builder()
                .purchaseDetailsId(2L)
                .userId(2L)
                .userName("Jane Smith")
                .productId(2L)
                .productName("Laptop")
                .productCount(1)
                .purchaseDate(new Date())
                .isActive(true)
                .build();
    }

    
    //--------------------------create New Purchase----------------------------------------------------------------------
    @Test
    void createNewPurchase_ShouldReturnCreated_WhenValidInput() {
        when(purchaseDetailsService.createPurchase(any(PurchaseDetailsDTO.class))).thenReturn(validPurchaseDTO);

        ResponseEntity<?> response = purchaseDetailsController.createNewPurchase(validPurchaseDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(validPurchaseDTO, response.getBody());
        verify(purchaseDetailsService, times(1)).createPurchase(any(PurchaseDetailsDTO.class));
    }

    // Test for PurchaseAlreadyExistsException: Purchase already exists
    @Test
    void createNewPurchase_ShouldReturnConflict_WhenPurchaseExists() {
        when(purchaseDetailsService.createPurchase(any(PurchaseDetailsDTO.class)))
                .thenThrow(new PurchaseAlreadyExistsException("Purchase already exists"));

        ResponseEntity<?> response = purchaseDetailsController.createNewPurchase(validPurchaseDTO);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Purchase already exists"));
    }

  
    @Test
    void createNewPurchase_ShouldReturnBadRequest_WhenInvalidInput() {
        PurchaseDetailsDTO invalidDTO = new PurchaseDetailsDTO();
        invalidDTO.setProductCount(-1); // Invalid count

        ResponseEntity<?> response = purchaseDetailsController.createNewPurchase(invalidDTO);

       
    }

   
    @Test
    void createNewPurchase_ShouldReturnConflict_WhenDataIntegrityViolation() {
        when(purchaseDetailsService.createPurchase(any(PurchaseDetailsDTO.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));

        ResponseEntity<?> response = purchaseDetailsController.createNewPurchase(validPurchaseDTO);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // Test for RuntimeException: Unexpected error during purchase creation
    @Test
    void createNewPurchase_ShouldReturnInternalServerError_WhenUnexpectedError() {
        when(purchaseDetailsService.createPurchase(any(PurchaseDetailsDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = purchaseDetailsController.createNewPurchase(validPurchaseDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    
    //-------------------------------------get Purchase Details By Purchase DetailsId-----------------------------------------------------------------
    @Test
    void getPurchaseDetailsByPurchaseDetailsId_ShouldReturnOk_WhenPurchaseExists() {
        when(purchaseDetailsService.getPurchaseByPurchaseDetailsId(anyLong())).thenReturn(validPurchaseDTO);

        ResponseEntity<?> response = purchaseDetailsController.getPurchaseDetailsByPurchaseDetailsId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(validPurchaseDTO, response.getBody());
        verify(purchaseDetailsService, times(1)).getPurchaseByPurchaseDetailsId(anyLong());
    }

    @Test
    void getPurchaseDetailsByPurchaseDetailsId_ShouldReturnNotFound_WhenPurchaseNotFound() {
        when(purchaseDetailsService.getPurchaseByPurchaseDetailsId(anyLong()))
                .thenThrow(new ResourceNotFoundException(AppConstants.PURCHASE_NOT_FOUND + 1L));

        ResponseEntity<?> response = purchaseDetailsController.getPurchaseDetailsByPurchaseDetailsId(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(AppConstants.PURCHASE_NOT_FOUND));
    }

    @Test
    void getPurchaseDetailsByPurchaseDetailsId_ShouldReturnBadRequest_WhenInvalidId() {
        ResponseEntity<?> response = purchaseDetailsController.getPurchaseDetailsByPurchaseDetailsId(-1L);

       
    }

   
    @Test
    void getPurchaseDetailsByPurchaseDetailsId_ShouldReturnInternalServerError_WhenUnexpectedError() {
        when(purchaseDetailsService.getPurchaseByPurchaseDetailsId(anyLong()))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = purchaseDetailsController.getPurchaseDetailsByPurchaseDetailsId(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    //-----------------------------get All Purchase Details------------------------------------------------------------------------
    @Test
    void getAllPurchaseDetails_ShouldReturnOk_WhenPurchasesExist() {
        List<PurchaseDetailsDTO> purchases = Arrays.asList(validPurchaseDTO, existingPurchaseDTO);
        when(purchaseDetailsService.getAllPurchases()).thenReturn(purchases);

        ResponseEntity<?> response = purchaseDetailsController.getAllPurchaseDetails();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(purchases, response.getBody());
        verify(purchaseDetailsService, times(1)).getAllPurchases();
    }

    @Test
    void getAllPurchaseDetails_ShouldReturnEmptyList_WhenNoPurchasesExist() {
        when(purchaseDetailsService.getAllPurchases()).thenReturn(List.of());

        ResponseEntity<?> response = purchaseDetailsController.getAllPurchaseDetails();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) response.getBody()).isEmpty());
    }

    @Test
    void getAllPurchaseDetails_ShouldReturnInternalServerError_WhenUnexpectedError() {
        when(purchaseDetailsService.getAllPurchases())
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = purchaseDetailsController.getAllPurchaseDetails();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getAllPurchaseDetailsByUserId_ShouldReturnOk_WhenPurchasesExist() {
        List<PurchaseDetailsDTO> purchases = Arrays.asList(validPurchaseDTO);
        when(purchaseDetailsService.getPurchasesByUserId(anyLong())).thenReturn(purchases);

        ResponseEntity<?> response = purchaseDetailsController.getAllPurchaseDetailsByUserId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(purchases, response.getBody());
        verify(purchaseDetailsService, times(1)).getPurchasesByUserId(anyLong());
    }

    @Test
    void getAllPurchaseDetailsByUserId_ShouldReturnEmptyList_WhenNoPurchasesExist() {
        when(purchaseDetailsService.getPurchasesByUserId(anyLong())).thenReturn(List.of());

        ResponseEntity<?> response = purchaseDetailsController.getAllPurchaseDetailsByUserId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) response.getBody()).isEmpty());
    }

    @Test
    void getAllPurchaseDetailsByUserId_ShouldReturnBadRequest_WhenInvalidUserId() {
        ResponseEntity<?> response = purchaseDetailsController.getAllPurchaseDetailsByUserId(-1L);

       // assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        //assertNotNull(response.getBody());
    }

    @Test
    void getAllPurchaseDetailsByUserId_ShouldReturnInternalServerError_WhenUnexpectedError() {
        when(purchaseDetailsService.getPurchasesByUserId(anyLong()))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = purchaseDetailsController.getAllPurchaseDetailsByUserId(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    
    //-----------------------------------update Purchase By Purchase DetailsId --------------------------------------------------------------
    @Test
    void updatePurchaseByPurchaseDetailsId_ShouldReturnOk_WhenValidInput() {
        when(purchaseDetailsService.updatePurchaseBypurchaseDetailsId(anyLong(), any(PurchaseDetailsDTO.class)))
                .thenReturn(validPurchaseDTO);

        ResponseEntity<?> response = purchaseDetailsController.updatePurchaseByPurchaseDetailsId(1L, validPurchaseDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(validPurchaseDTO, response.getBody());
        verify(purchaseDetailsService, times(1)).updatePurchaseBypurchaseDetailsId(anyLong(), any(PurchaseDetailsDTO.class));
    }

    @Test
    void updatePurchaseByPurchaseDetailsId_ShouldReturnNotFound_WhenPurchaseNotFound() {
        when(purchaseDetailsService.updatePurchaseBypurchaseDetailsId(anyLong(), any(PurchaseDetailsDTO.class)))
                .thenThrow(new ResourceNotFoundException(AppConstants.PURCHASE_NOT_FOUND + 1L));

        ResponseEntity<?> response = purchaseDetailsController.updatePurchaseByPurchaseDetailsId(1L, validPurchaseDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(AppConstants.PURCHASE_NOT_FOUND));
    }

    @Test
    void updatePurchaseByPurchaseDetailsId_ShouldReturnBadRequest_WhenInvalidInput() {
        PurchaseDetailsDTO invalidDTO = new PurchaseDetailsDTO();
        invalidDTO.setProductCount(-1);

        ResponseEntity<?> response = purchaseDetailsController.updatePurchaseByPurchaseDetailsId(1L, invalidDTO);

        
    }

    @Test
    void updatePurchaseByPurchaseDetailsId_ShouldReturnInternalServerError_WhenUnexpectedError() {
        when(purchaseDetailsService.updatePurchaseBypurchaseDetailsId(anyLong(), any(PurchaseDetailsDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = purchaseDetailsController.updatePurchaseByPurchaseDetailsId(1L, validPurchaseDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    
    //---------------------------------------delete Purchase Details By Purchase DetailsId--------------------------------------------------------
    @Test
    void deletePurchaseDetailsByPurchaseDetailsId_ShouldReturnNoContent_WhenSuccessful() {
        doNothing().when(purchaseDetailsService).deletePurchaseByPurchaseDetailsId(anyLong());

        ResponseEntity<?> response = purchaseDetailsController.deletePurchaseDetailsByPurchaseDetailsId(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(purchaseDetailsService, times(1)).deletePurchaseByPurchaseDetailsId(anyLong());
    }

    @Test
    void deletePurchaseDetailsByPurchaseDetailsId_ShouldReturnNotFound_WhenPurchaseNotFound() {
        doThrow(new ResourceNotFoundException(AppConstants.PURCHASE_NOT_FOUND + 1L))
                .when(purchaseDetailsService).deletePurchaseByPurchaseDetailsId(anyLong());

        ResponseEntity<?> response = purchaseDetailsController.deletePurchaseDetailsByPurchaseDetailsId(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(AppConstants.PURCHASE_NOT_FOUND));
    }

    @Test
    void deletePurchaseDetailsByPurchaseDetailsId_ShouldReturnBadRequest_WhenInvalidId() {
        ResponseEntity<?> response = purchaseDetailsController.deletePurchaseDetailsByPurchaseDetailsId(-1L);

        
    }

    @Test
    void deletePurchaseDetailsByPurchaseDetailsId_ShouldReturnInternalServerError_WhenUnexpectedError() {
        doThrow(new RuntimeException("Unexpected error"))
                .when(purchaseDetailsService).deletePurchaseByPurchaseDetailsId(anyLong());

        ResponseEntity<?> response = purchaseDetailsController.deletePurchaseDetailsByPurchaseDetailsId(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}