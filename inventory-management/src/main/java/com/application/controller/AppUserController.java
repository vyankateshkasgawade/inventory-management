package com.application.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.application.dto.AppUserDTO;
import com.application.dto.AddressDTO; // Import AddressDTO
import com.application.dto.RegistrationRequest;
import com.application.entity.AppUser;
import com.application.entity.Address; // Import Address entity
import com.application.repository.AppUserRepository;
import com.application.service.AppUserService;
import com.application.service.OtpStoreService;
import com.application.util.AppConstants;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AppUserController", description = "Operations related to User management")
public class AppUserController {

    private final PasswordEncoder passwordEncoder;

    private final AppUserRepository appUserRepository;

    private final AppUserService appUserService;
    
    private final OtpStoreService otpStoreService;

    
    
   
    @PostMapping("/register")
    public ResponseEntity<String> registerNewUser(@RequestBody RegistrationRequest request) {
        // Step 1: Verify OTP via injected service instance
        boolean isOtpValid = otpStoreService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isOtpValid) {
            return ResponseEntity.status(400).body("Invalid OTP. Please verify your email.");
        }

        // Step 2: Check if user already exists
        if (appUserRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(400).body("Email already registered.");
        }

        AppUser user = new AppUser();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setDob(request.getDob());
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setIsActive(true);

        // Handle Address
        if (request.getAddress() != null) {
            Address address = new Address();
            AddressDTO addressDTO = request.getAddress();
            address.setCountryRegion(addressDTO.getCountryRegion());
            address.setPincode(addressDTO.getPincode());
            address.setFlatHouseNoBuildingCompanyApartment(addressDTO.getFlatHouseNoBuildingCompanyApartment());
            address.setAreaStreetSectorVillage(addressDTO.getAreaStreetSectorVillage());
            address.setLandmark(addressDTO.getLandmark());
            address.setTownCity(addressDTO.getTownCity());
            address.setState(addressDTO.getState());
            address.setUser(user); // Set the user for the address
            user.setAddress(address); // Set the address for the user
        }
        
        appUserRepository.save(user);

        // Step 4: Remove OTP after successful registration
        otpStoreService.removeOtp(request.getEmail());

        return ResponseEntity.ok("User registered successfully.");
    }



    //@PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{userId}")
    @Operation(summary = "Get User by ID", description = "Fetch a user by their unique user ID")
    public ResponseEntity<AppUserDTO> getUserByUserId(@PathVariable Long userId) 
    {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Fetching user by ID: {}", userId);
        
        AppUserDTO user = appUserService.getUserByUserId(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    
    //@PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    @Operation(summary = "Get All Users", description = "Fetch all users in the system")
    public ResponseEntity<List<AppUserDTO>> getAllAppUsers() 
    {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Fetching all users");
        List<AppUserDTO> users = appUserService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    
    //@PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PutMapping("/{userId}")
    @Operation(summary = "Update User by ID", description = "Update an existing user by their unique user ID")
    public ResponseEntity<AppUserDTO> updateUserByUserId(@PathVariable Long userId, @RequestBody AppUserDTO userDTO) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Updating user by ID: {}", userId);
        AppUserDTO updatedUser = appUserService.updateUserByUserId(userId, userDTO);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    
    
    //@PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete User by ID", description = "Delete a user by their unique user ID")
    public ResponseEntity<Void> deleteUserByUserId(@PathVariable Long userId)
    {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Deleting user by ID: {}", userId);
        appUserService.DeleteUserByUserId(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}