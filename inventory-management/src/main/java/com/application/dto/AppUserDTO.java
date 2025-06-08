package com.application.dto;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonProperty;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUserDTO {
    private Long userId;
    private String name;
    private String email;
    private String dob;
    private String phone;
    private AddressDTO address; 
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    
    private Boolean isActive;
    private String role;
}