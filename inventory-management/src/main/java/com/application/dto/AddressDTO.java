package com.application.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO
{
    private String countryRegion;
    
    private String pincode;
    
    private String flatHouseNoBuildingCompanyApartment;
    
    private String areaStreetSectorVillage;
    
    private String landmark;
    
    private String townCity;
    
    private String state;
}
