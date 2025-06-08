package com.application.service.impl;

import com.application.dto.ProductCategoryDTO;
import com.application.entity.ProductCategory;
import com.application.exception.ProductCategoryException;
import com.application.exception.ResourceNotFoundException;
import com.application.exception.UserNotFoundException;
import com.application.repository.ProductCategoryRepository;
import com.application.service.ProductCategoryService;
import com.application.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCategoryServiceImpl implements ProductCategoryService
{

    private final ProductCategoryRepository productCategoryRepository;
    
//============================================create Category ============================================================================
    @Override
    public ProductCategoryDTO createCategory(ProductCategoryDTO categoryDTO) 
    {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Creating new Product Category: {}", categoryDTO.getProductCategoryName());
        try {
            Optional<ProductCategory> existingCategory = productCategoryRepository.findByProductCategoryNameAndIsActiveTrue(categoryDTO.getProductCategoryName());
            if (existingCategory.isPresent()) 
            {
                throw new ProductCategoryException("Product Category with name '" + categoryDTO.getProductCategoryName() + "' already exists.");
            }

            ProductCategory category = mapToEntity(categoryDTO);
            category.setIsActive(true);
            return mapToDTO(productCategoryRepository.save(category));
        } catch (IllegalArgumentException e) 
        {
            log.error("Invalid category input: {}", categoryDTO, e);
            throw e;
        } catch (ProductCategoryException | ResourceNotFoundException e)
        {
            throw e;
        } catch (Exception e) 
        {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Error creating Product Category: {}", categoryDTO.getProductCategoryName(), e);
            throw new ProductCategoryException(AppConstants.SOMETHING_WENT_WRONG + " " + e.getMessage(), e);
        }
    }


    //============================================get Category By Category Id ============================================================================
    @Override
    public ProductCategoryDTO getCategoryByCategoryId(Long categoryId)
    {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Fetching Product Category by ID: {}", categoryId);
        try {
            ProductCategory category = productCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.CATEGORY_NOT_FOUND + categoryId));
            
            if (!category.getIsActive()) {
                throw new ResourceNotFoundException(AppConstants.CATEGORY_NOT_FOUND + categoryId);
            }


            return mapToDTO(category);
        } catch (ProductCategoryException | ResourceNotFoundException e)
        {
            throw e;
        } 
    }

    
  //============================================get All Categories ============================================================================
    @Override
    public List<ProductCategoryDTO> getAllCategories()
    {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Fetching all Product Categories");
        try {
            return productCategoryRepository.findAll().stream().filter(c -> c.getIsActive()==true).map(this::mapToDTO).collect(Collectors.toList());
        } catch (ProductCategoryException e)
        {
            throw e;
        } 
    }

    
  //============================================update Category By CategoryId ============================================================================
    @Override
    public ProductCategoryDTO updateCategoryByCategoryId(Long categoryId, ProductCategoryDTO categoryDTO)
    {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Updating Product Category by ID: {}", categoryId);
        try {
            ProductCategory existing = productCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.CATEGORY_NOT_FOUND + categoryId));

            existing.setProductCategoryName(categoryDTO.getProductCategoryName());
            return mapToDTO(productCategoryRepository.save(existing));
        } catch (ProductCategoryException | ResourceNotFoundException e)
        {
            throw e;
        } 
    }

    
  //============================================delete Category By Category Id ============================================================================ 
    @Override
    public void deleteCategoryByCategoryId(Long categoryId) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Deleting Product Category by ID: {}", categoryId);
        try {
            ProductCategory category = productCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.CATEGORY_NOT_FOUND + categoryId));
            category.setIsActive(false);
            productCategoryRepository.save(category);
            //productCategoryRepository.delete(category);
     
        } catch (ProductCategoryException | ResourceNotFoundException e)
        {
            throw e;
        } 
    }

    
    
    private ProductCategory mapToEntity(ProductCategoryDTO dto) 
    {
        return ProductCategory.builder()
                .productCategoryId(dto.getProductCategoryId())
                .productCategoryName(dto.getProductCategoryName())
                .build();
    }

    private ProductCategoryDTO mapToDTO(ProductCategory entity)
    {
        return ProductCategoryDTO.builder()
                .productCategoryId(entity.getProductCategoryId())
                .productCategoryName(entity.getProductCategoryName())
                .isActive(entity.getIsActive())
                .build();
        		
        
    }
}
