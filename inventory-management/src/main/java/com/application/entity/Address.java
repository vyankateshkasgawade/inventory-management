package com.application.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    private String countryRegion;
    private String pincode;
    private String flatHouseNoBuildingCompanyApartment;
    private String areaStreetSectorVillage;
    private String landmark;
    private String townCity;
    private String state;

    // FIX: Changed mappedBy from "user" to "address"
    @OneToOne(mappedBy = "address") 
    private AppUser user; 
}