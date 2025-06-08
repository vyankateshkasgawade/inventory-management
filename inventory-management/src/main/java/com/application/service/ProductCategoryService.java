package com.application.service;




import java.util.List;

import com.application.dto.ProductCategoryDTO;

public interface ProductCategoryService 
{
    ProductCategoryDTO createCategory(ProductCategoryDTO categoryDTO);
    ProductCategoryDTO updateCategoryByCategoryId(Long categoryId, ProductCategoryDTO categoryDTO);
    ProductCategoryDTO getCategoryByCategoryId(Long categoryId);
    List<ProductCategoryDTO> getAllCategories();
    void deleteCategoryByCategoryId(Long categoryId);
}
