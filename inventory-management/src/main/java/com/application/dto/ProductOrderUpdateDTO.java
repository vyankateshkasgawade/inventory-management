package com.application.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrderUpdateDTO {
    private Long id;
    private Integer displayOrder; // Can be null to remove from the list
}