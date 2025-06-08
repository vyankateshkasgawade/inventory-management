package com.application.controller;

import com.application.dto.AppUserDTO;
import com.application.dto.AddressDTO;
import com.application.dto.RegistrationRequest;
import com.application.entity.AppUser;
import com.application.entity.Address;
import com.application.exception.UserNotFoundException;
import com.application.exception.ValidationException;
import com.application.repository.AppUserRepository;
import com.application.service.AppUserService;
import com.application.service.OtpStoreService;
import com.application.util.AppConstants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor; // Import ArgumentCaptor
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
public class AppUserControllerTest {

    @Mock
    private AppUserService appUserService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private OtpStoreService otpStoreService;

    @InjectMocks
    private AppUserController appUserController;

    private AppUserDTO testUserDTO;
    private RegistrationRequest registrationRequest;
    private AppUser testAppUser; // Used for mocking repository responses

    private static final String VALID_DOB_STRING = "1990-01-15";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testUserDTO = AppUserDTO.builder()
                .userId(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .build();

        registrationRequest = new RegistrationRequest();
        registrationRequest.setName("Register User");
        registrationRequest.setEmail("register@example.com");
        registrationRequest.setPassword("rawPassword");
        registrationRequest.setOtp("123456");
        registrationRequest.setPhone("1234567890");
        registrationRequest.setDob(VALID_DOB_STRING);
        registrationRequest.setRole("USER"); // Default role for most tests

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountryRegion("India");
        addressDTO.setPincode("411001");
        addressDTO.setFlatHouseNoBuildingCompanyApartment("Flat 101");
        addressDTO.setAreaStreetSectorVillage("Main Street");
        addressDTO.setLandmark("Near School");
        addressDTO.setTownCity("Pune");
        addressDTO.setState("Maharashtra");
        registrationRequest.setAddress(addressDTO); // Set address for most tests

        testAppUser = AppUser.builder()
                .userId(1L)
                .name("Register User")
                .email("register@example.com")
                .password("encodedPassword")
                .phone("1234567890")
                .dob(VALID_DOB_STRING)
                .role("USER")
                .isActive(true)
                .build();
        Address testAddress = new Address();
        testAddress.setCountryRegion("India");
        testAddress.setPincode("411001");
        testAddress.setFlatHouseNoBuildingCompanyApartment("Flat 101");
        testAddress.setAreaStreetSectorVillage("Main Street");
        testAddress.setLandmark("Near School");
        testAddress.setTownCity("Pune");
        testAddress.setState("Maharashtra");
        testAppUser.setAddress(testAddress);
        testAddress.setUser(testAppUser);
    }

    // ================= Register User Tests =================

    @Test
    public void testRegisterNewUser_Success() {
        // Given
        when(otpStoreService.verifyOtp(registrationRequest.getEmail(), registrationRequest.getOtp())).thenReturn(true);
        when(appUserRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPasswordFromMock");

        // Use ArgumentCaptor to capture the AppUser object passed to save()
        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        when(appUserRepository.save(userCaptor.capture())).thenReturn(testAppUser); // Return a valid user

        // When
        ResponseEntity<String> response = appUserController.registerNewUser(registrationRequest);
    }

    @Test
    public void testRegisterNewUser_InvalidOtp() {
        // Given
        when(otpStoreService.verifyOtp(registrationRequest.getEmail(), registrationRequest.getOtp())).thenReturn(false);

        // When
        ResponseEntity<String> response = appUserController.registerNewUser(registrationRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid OTP. Please verify your email.", response.getBody());

    }

    @Test
    public void testRegisterNewUser_EmailAlreadyRegistered() {
        // Given
        when(otpStoreService.verifyOtp(registrationRequest.getEmail(), registrationRequest.getOtp())).thenReturn(true);
        when(appUserRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.of(testAppUser));

        // When
        ResponseEntity<String> response = appUserController.registerNewUser(registrationRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      verify(otpStoreService, never()).removeOtp(anyString());
    }

    @Test
    public void testRegisterNewUser_SaveThrowsException() {
        // Given
        when(otpStoreService.verifyOtp(registrationRequest.getEmail(), registrationRequest.getOtp())).thenReturn(true);
        when(appUserRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPasswordFromMock");
        when(appUserRepository.save(any(AppUser.class))).thenThrow(new RuntimeException("Database connection failed"));

        // When
        ResponseEntity<String> response = appUserController.registerNewUser(registrationRequest);

       verify(otpStoreService, never()).removeOtp(anyString());
    }

    @Test
    public void testRegisterNewUser_NoAddressProvided() {
        // Given
        registrationRequest.setAddress(null); // Explicitly set address to null

        when(otpStoreService.verifyOtp(registrationRequest.getEmail(), registrationRequest.getOtp())).thenReturn(true);
        when(appUserRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPasswordFromMock");

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        when(appUserRepository.save(userCaptor.capture())).thenReturn(testAppUser);

        // When
        ResponseEntity<String> response = appUserController.registerNewUser(registrationRequest);

       // verify(otpStoreService).removeOtp(registrationRequest.getEmail());
    }

    @Test
    public void testRegisterNewUser_RoleIsNull_DefaultToUser() {
        // Given
        registrationRequest.setRole(null); // Set role to null to test default assignment

        when(otpStoreService.verifyOtp(registrationRequest.getEmail(), registrationRequest.getOtp())).thenReturn(true);
        when(appUserRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPasswordFromMock");

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        when(appUserRepository.save(userCaptor.capture())).thenReturn(testAppUser);

        // When
        ResponseEntity<String> response = appUserController.registerNewUser(registrationRequest);

     // verify(otpStoreService).removeOtp(registrationRequest.getEmail());
    }

    // ================= Get User By ID =================

    @Test
    public void testGetUserByUserId_Success() {
        when(appUserService.getUserByUserId(1L)).thenReturn(testUserDTO);
        ResponseEntity<AppUserDTO> response = appUserController.getUserByUserId(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
      
    }

    @Test
    public void testGetUserByUserId_UserNotFoundException() {
        when(appUserService.getUserByUserId(1L)).thenThrow(new UserNotFoundException("User not found"));
        ResponseEntity<AppUserDTO> response = appUserController.getUserByUserId(1L);
        //verify(appUserService).getUserByUserId(1L);
    }

    @Test
    public void testGetUserByUserId_Exception() {
        doThrow(new RuntimeException("DB error")).when(appUserService).getUserByUserId(1L);
        ResponseEntity<AppUserDTO> response = appUserController.getUserByUserId(1L);
      
    }

    // ================= Get All Users =================

    @Test
    public void testGetAllAppUsers_Success() {
        List<AppUserDTO> users = Arrays.asList(testUserDTO, AppUserDTO.builder().userId(2L).name("Jane Doe").email("jane@example.com").build());
        when(appUserService.getAllUsers()).thenReturn(users);
        ResponseEntity<List<AppUserDTO>> response = appUserController.getAllAppUsers();
        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void testGetAllAppUsers_Exception() {
        doThrow(new RuntimeException("Service error")).when(appUserService).getAllUsers();
        ResponseEntity<List<AppUserDTO>> response = appUserController.getAllAppUsers();

    }

    // ================= Update User =================

    @Test
    public void testUpdateUserByUserId_Success() {
        AppUserDTO updatedDto = AppUserDTO.builder().userId(1L).name("Updated Name").build();
        when(appUserService.updateUserByUserId(eq(1L), any(AppUserDTO.class))).thenReturn(updatedDto);
        ResponseEntity<AppUserDTO> response = appUserController.updateUserByUserId(1L, testUserDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void testUpdateUserByUserId_UserNotFound() {
        when(appUserService.updateUserByUserId(eq(1L), any(AppUserDTO.class))).thenThrow(new UserNotFoundException("User not found for update"));
        ResponseEntity<AppUserDTO> response = appUserController.updateUserByUserId(1L, testUserDTO);

    }

    @Test
    public void testUpdateUserByUserId_ValidationException() {
        doThrow(new ValidationException("Invalid email format")).when(appUserService).updateUserByUserId(eq(1L), any(AppUserDTO.class));
        ResponseEntity<AppUserDTO> response = appUserController.updateUserByUserId(1L, testUserDTO);

    }

    @Test
    public void testUpdateUserByUserId_DataIntegrityViolationException() {
        doThrow(new DataIntegrityViolationException("Duplicate email during update")).when(appUserService).updateUserByUserId(eq(1L), any(AppUserDTO.class));
        ResponseEntity<AppUserDTO> response = appUserController.updateUserByUserId(1L, testUserDTO);
    }

    @Test
    public void testUpdateUserByUserId_Exception() {
        doThrow(new RuntimeException("Update error")).when(appUserService).updateUserByUserId(eq(1L), any(AppUserDTO.class));
        ResponseEntity<AppUserDTO> response = appUserController.updateUserByUserId(1L, testUserDTO);

    }

    // ================= Delete User =================

    @Test
    public void testDeleteUserByUserId_Success() {
        doNothing().when(appUserService).DeleteUserByUserId(1L);
        ResponseEntity<Void> response = appUserController.deleteUserByUserId(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        //verify(appUserService).DeleteUserByUserId(1L);
    }

    @Test
    public void testDeleteUserByUserId_UserNotFoundException() {
        doThrow(new UserNotFoundException("User not found for deletion")).when(appUserService).DeleteUserByUserId(1L);
        ResponseEntity<Void> response = appUserController.deleteUserByUserId(1L);

    }

    @Test
    public void testDeleteUserByUserId_ValidationException() {
        doThrow(new ValidationException("Invalid user ID for deletion")).when(appUserService).DeleteUserByUserId(1L);
        ResponseEntity<Void> response = appUserController.deleteUserByUserId(1L);
    }

    @Test
    public void testDeleteUserByUserId_Exception() {
        doThrow(new RuntimeException("Delete failed")).when(appUserService).DeleteUserByUserId(1L);
        ResponseEntity<Void> response = appUserController.deleteUserByUserId(1L);
    }
}