package com.application.service.impl;

import com.application.dto.AppUserDTO;
import com.application.dto.AddressDTO;
import com.application.entity.AppUser;
import com.application.entity.Address;
import com.application.exception.UserAlreadyExistsException;
import com.application.exception.UserNotFoundException;
import com.application.exception.ValidationException;
import com.application.repository.AppUserRepository;
import com.application.repository.AddressRepository; // Import AddressRepository
import com.application.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppUserServiceImplTest {

    @InjectMocks
    private AppUserServiceImpl appUserService;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock // Add mock for AddressRepository
    private AddressRepository addressRepository;

    private AppUserDTO validUserDTO;
    private AppUser validUser;
    private AppUserDTO userDTOWithAddress;
    private AppUser userWithAddress;
    private AddressDTO testAddressDTO;
    private Address testAddressEntity;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testAddressDTO = AddressDTO.builder()
                .countryRegion("India")
                .pincode("411001")
                .flatHouseNoBuildingCompanyApartment("101, A Block")
                .areaStreetSectorVillage("Main Street")
                .landmark("Near City Center")
                .townCity("Pune")
                .state("Maharashtra")
                .build();

        testAddressEntity = Address.builder()
                .addressId(1L) // Assuming it gets an ID after saving
                .countryRegion("India")
                .pincode("411001")
                .flatHouseNoBuildingCompanyApartment("101, A Block")
                .areaStreetSectorVillage("Main Street")
                .landmark("Near City Center")
                .townCity("Pune")
                .state("Maharashtra")
                .build();

        validUserDTO = AppUserDTO.builder()
                .userId(null) // userId typically null for new registration DTO
                .name("Test User")
                .email("test@example.com")
                .dob("2000-01-01") // Changed to YYYY-MM-DD for consistency
                .phone("1234567890")
                .password("password123")
                .role("USER")
                .isActive(true) // Default to true if not explicitly set in DTO
                .build();

        validUser = AppUser.builder()
                .userId(1L)
                .name("Test User")
                .email("test@example.com")
                .dob("2000-01-01")
                .phone("1234567890")
                .password("encodedPassword")
                .role("USER")
                .isActive(true)
                .build();
        
        // User DTO with address
        userDTOWithAddress = AppUserDTO.builder()
                .userId(null)
                .name("User With Address")
                .email("address@example.com")
                .dob("1995-03-10")
                .phone("9876543210")
                .password("passWithAddress")
                .role("USER")
                .isActive(true)
                .address(testAddressDTO)
                .build();

        // Corresponding User entity with address
        userWithAddress = AppUser.builder()
                .userId(2L)
                .name("User With Address")
                .email("address@example.com")
                .dob("1995-03-10")
                .phone("9876543210")
                .password("encodedPassWithAddress")
                .role("USER")
                .isActive(true)
                .address(testAddressEntity) // Link the address entity
                .build();
        
        testAddressEntity.setUser(userWithAddress); // Set back-reference for entity
    }

    // --- registerUser tests ---

    @Test
    void registerUser_ShouldRegisterNewUser_WhenValidInput() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        // Mock the save operation to return the validUser with an ID
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser userToSave = invocation.getArgument(0);
            userToSave.setUserId(1L); // Simulate ID generation by DB
            return userToSave;
        });

        AppUserDTO savedUser = appUserService.registerUser(validUserDTO);

        assertThat(savedUser.getEmail()).isEqualTo(validUserDTO.getEmail());
        assertThat(savedUser.getUserId()).isNotNull();
        assertThat(savedUser.getIsActive()).isTrue();
        assertThat(savedUser.getRole()).isEqualTo("USER");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void registerUser_ShouldSetDefaultRoleToUser_WhenRoleIsNull() {
        validUserDTO.setRole(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser userToSave = invocation.getArgument(0);
            userToSave.setUserId(1L);
            return userToSave;
        });

        AppUserDTO savedUser = appUserService.registerUser(validUserDTO);
        assertThat(savedUser.getRole()).isEqualTo("USER");
        verify(userRepository).save(argThat(user -> "USER".equals(user.getRole())));
    }

    @Test
    void registerUser_ShouldSetDefaultRoleToUser_WhenRoleIsEmpty() {
        validUserDTO.setRole("");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser userToSave = invocation.getArgument(0);
            userToSave.setUserId(1L);
            return userToSave;
        });

        AppUserDTO savedUser = appUserService.registerUser(validUserDTO);
        assertThat(savedUser.getRole()).isEqualTo("USER");
        verify(userRepository).save(argThat(user -> "USER".equals(user.getRole())));
    }

    @Test
    void registerUser_ShouldThrowValidationException_WhenNameIsNull() {
        validUserDTO.setName(null);
        assertThatThrownBy(() -> appUserService.registerUser(validUserDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Username, email, and phone must not be null.");
        verifyNoInteractions(userRepository); // Ensure no interaction with repo if validation fails early
    }

    @Test
    void registerUser_ShouldThrowValidationException_WhenEmailIsNull() {
        validUserDTO.setEmail(null);
        assertThatThrownBy(() -> appUserService.registerUser(validUserDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Username, email, and phone must not be null.");
        verifyNoInteractions(userRepository);
    }

    @Test
    void registerUser_ShouldThrowValidationException_WhenPhoneIsNull() {
        validUserDTO.setPhone(null);
        assertThatThrownBy(() -> appUserService.registerUser(validUserDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Username, email, and phone must not be null.");
        verifyNoInteractions(userRepository);
    }

    @Test
    void registerUser_ShouldThrowUserAlreadyExistsException_WhenEmailExists() {
        when(userRepository.findByEmail(validUserDTO.getEmail())).thenReturn(Optional.of(validUser));

        assertThatThrownBy(() -> appUserService.registerUser(validUserDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with email '" + validUserDTO.getEmail() + "' already exists.");
        verify(userRepository, never()).findByPhone(anyString()); // Should not check phone if email exists
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void registerUser_ShouldThrowUserAlreadyExistsException_WhenPhoneExists() {
        when(userRepository.findByEmail(validUserDTO.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(validUserDTO.getPhone())).thenReturn(Optional.of(validUser));

        assertThatThrownBy(() -> appUserService.registerUser(validUserDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with phone number '" + validUserDTO.getPhone() + "' already exists.");
        verify(userRepository).findByEmail(validUserDTO.getEmail());
        verify(userRepository).findByPhone(validUserDTO.getPhone());
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void registerUser_ShouldHandleDataIntegrityViolationException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(AppUser.class))).thenThrow(new DataIntegrityViolationException("DB error"));

        assertThatThrownBy(() -> appUserService.registerUser(validUserDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error: DB error");
    }

    @Test
    void registerUser_ShouldHandleGenericException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(AppUser.class))).thenThrow(new RuntimeException("Something unexpected"));

        assertThatThrownBy(() -> appUserService.registerUser(validUserDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(AppConstants.SOMETHING_WENT_WRONG);
    }
    
    @Test
    void registerUser_ShouldRegisterUserWithAddress_WhenAddressProvided() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassWithAddress");
        
//        // Mock save to return the user with its address (simulating DB saving both)
//        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
//            AppUser user = invocation.getArgument(0);
//            user.setUserId(2L); // Simulate ID generation for user
//            if (user.getAddress() != null) {
//                user.getAddress().setAddressId(1L); // Simulate ID generation for address
//            }
//            return user;
//        });

//        AppUserDTO savedUserDTO = appUserService.registerUser(userDTOWithAddress);
//
//        assertThat(savedUserDTO).isNotNull();
//        assertThat(savedUserDTO.getEmail()).isEqualTo(userDTOWithAddress.getEmail());
//        assertThat(savedUserDTO.getAddress()).isNotNull();
//        assertThat(savedUserDTO.getAddress().getPincode()).isEqualTo(testAddressDTO.getPincode());
//        
//        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
//        verify(userRepository).save(userCaptor.capture());
//        
//        AppUser capturedUser = userCaptor.getValue();
//        assertThat(capturedUser.getAddress()).isNotNull();
//        assertThat(capturedUser.getAddress().getUser()).isEqualTo(capturedUser); // Verify back-reference
//        assertThat(capturedUser.getAddress().getPincode()).isEqualTo(testAddressDTO.getPincode());
    }


    // --- getUserByUserId tests ---

    @Test
    void getUserByUserId_ShouldReturnUser_WhenValidAndActiveId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        AppUserDTO result = appUserService.getUserByUserId(1L);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo(validUser.getEmail());
        assertThat(result.getIsActive()).isTrue();
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserByUserId_ShouldThrowValidationException_WhenUserIdIsNull() {
        assertThatThrownBy(() -> appUserService.getUserByUserId(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid userId provided.");
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserByUserId_ShouldThrowValidationException_WhenUserIdIsZero() {
        assertThatThrownBy(() -> appUserService.getUserByUserId(0L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid userId provided.");
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserByUserId_ShouldThrowValidationException_WhenUserIdIsNegative() {
        assertThatThrownBy(() -> appUserService.getUserByUserId(-1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid userId provided.");
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserByUserId_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appUserService.getUserByUserId(2L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(AppConstants.USER_NOT_FOUND + 2L);
    }

    @Test
    void getUserByUserId_ShouldThrowUserNotFoundException_WhenUserIsInactive() {
        AppUser inactiveUser = AppUser.builder()
                .userId(3L)
                .email("inactive@example.com")
                .isActive(false)
                .build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> appUserService.getUserByUserId(3L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(AppConstants.USER_NOT_FOUND + 3L);
    }

    @Test
    void getUserByUserId_ShouldHandleGenericException() {
        when(userRepository.findById(1L)).thenThrow(new RuntimeException("DB connection failed"));
        assertThatThrownBy(() -> appUserService.getUserByUserId(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(AppConstants.SOMETHING_WENT_WRONG);
    }

    // --- getAllUsers tests ---

    @Test
    void getAllUsers_ShouldReturnOnlyActiveUsers() {
        AppUser inactiveUser = AppUser.builder().userId(2L).email("inactive@test.com").isActive(false).build();
        when(userRepository.findAll()).thenReturn(Arrays.asList(validUser, inactiveUser));

        List<AppUserDTO> users = appUserService.getAllUsers();
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo(validUser.getEmail());
        assertThat(users.get(0).getIsActive()).isTrue();
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsers() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<AppUserDTO> users = appUserService.getAllUsers();
        assertThat(users).isEmpty();
    }

//    @Test
//    void getAllUsers_ShouldHandleGenericException() {
//        when(userRepository.findAll()).thenThrow(new RuntimeException("Failed to fetch from DB"));
//        assertThatThrownBy(() -> appUserService.getAllUsers())
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining(AppConstants.SOMETHING_WENT_WRONG);
//    }

    // --- updateUserByUserId tests ---

    @Test
    void updateUserByUserId_ShouldUpdateUserFields_WhenValidInput() {
        AppUser existingUser = AppUser.builder()
                .userId(1L)
                .name("Old Name")
                .email("old@example.com")
                .dob("1980-01-01")
                .phone("0000000000")
                .password("oldEncoded")
                .role("USER")
                .isActive(true)
                .build();
        
        AppUserDTO updateDTO = AppUserDTO.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .dob("1985-05-05")
                .phone("1111111111")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUserDTO result = appUserService.updateUserByUserId(1L, updateDTO);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getDob()).isEqualTo("1985-05-05");
        assertThat(result.getPhone()).isEqualTo("1111111111");
        
        // Verify save was called with the updated entity
        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(userCaptor.capture());
        AppUser savedUser = userCaptor.getValue();
        assertThat(savedUser.getName()).isEqualTo("Updated Name");
        assertThat(savedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void updateUserByUserId_ShouldAddAddress_WhenExistingUserHasNoAddressAndDTOHasAddress() {
        AppUser existingUserNoAddress = AppUser.builder()
                .userId(1L)
                .name("No Address User")
                .email("noaddress@example.com")
                .isActive(true)
                .build();
        
        AppUserDTO updateDTOWithAddress = AppUserDTO.builder()
                .name("No Address User")
                .email("noaddress@example.com")
                .address(testAddressDTO)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUserNoAddress));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser savedUser = invocation.getArgument(0);
            if (savedUser.getAddress() != null) {
                savedUser.getAddress().setAddressId(1L); // Simulate address ID generation
            }
            return savedUser;
        });

        AppUserDTO result = appUserService.updateUserByUserId(1L, updateDTOWithAddress);

        assertThat(result.getAddress()).isNotNull();
        assertThat(result.getAddress().getPincode()).isEqualTo(testAddressDTO.getPincode());
        
        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(userCaptor.capture());
        AppUser savedUser = userCaptor.getValue();
        assertThat(savedUser.getAddress()).isNotNull();
        assertThat(savedUser.getAddress().getUser()).isEqualTo(savedUser); // Check back-reference
    }

    @Test
    void updateUserByUserId_ShouldUpdateExistingAddress_WhenDTOHasAddress() {
        Address existingAddress = Address.builder()
                .addressId(10L)
                .countryRegion("Old Country")
                .pincode("OLD123")
                .build();
        AppUser existingUserWithAddress = AppUser.builder()
                .userId(1L)
                .name("Old Address User")
                .email("oldaddress@example.com")
                .isActive(true)
                .address(existingAddress)
                .build();
        existingAddress.setUser(existingUserWithAddress); // Set back-reference

        AddressDTO updatedAddressDTO = AddressDTO.builder()
                .countryRegion("New Country")
                .pincode("NEW456")
                .build();
        AppUserDTO updateDTO = AppUserDTO.builder()
                .name("Old Address User")
                .email("oldaddress@example.com")
                .address(updatedAddressDTO)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUserWithAddress));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUserDTO result = appUserService.updateUserByUserId(1L, updateDTO);

        assertThat(result.getAddress()).isNotNull();
        assertThat(result.getAddress().getCountryRegion()).isEqualTo("New Country");
        assertThat(result.getAddress().getPincode()).isEqualTo("NEW456");
        
        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(userCaptor.capture());
        AppUser savedUser = userCaptor.getValue();
        assertThat(savedUser.getAddress().getAddressId()).isEqualTo(10L); // ID should remain same
        assertThat(savedUser.getAddress().getCountryRegion()).isEqualTo("New Country");
        assertThat(savedUser.getAddress().getUser()).isEqualTo(savedUser); // Back-reference still points to user
    }

    @Test
    void updateUserByUserId_ShouldRemoveAddress_WhenDTOHasNullAddressAndUserHasExistingAddress() {
        Address existingAddress = Address.builder()
                .addressId(10L)
                .countryRegion("Existing Country")
                .pincode("EXIST")
                .build();
        AppUser existingUserWithAddress = AppUser.builder()
                .userId(1L)
                .name("User With Address")
                .email("user@example.com")
                .isActive(true)
                .address(existingAddress)
                .build();
        existingAddress.setUser(existingUserWithAddress);

        AppUserDTO updateDTO = AppUserDTO.builder()
                .name("User With Address")
                .email("user@example.com")
                .address(null) // Request to remove address
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUserWithAddress));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUserDTO result = appUserService.updateUserByUserId(1L, updateDTO);

        assertThat(result.getAddress()).isNull();
        verify(addressRepository).delete(existingAddress); // Verify address was deleted
        
        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(userCaptor.capture());
        AppUser savedUser = userCaptor.getValue();
        assertThat(savedUser.getAddress()).isNull();
    }

    @Test
    void updateUserByUserId_ShouldThrowValidationException_WhenUserIdIsNull() {
        assertThatThrownBy(() -> appUserService.updateUserByUserId(null, validUserDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid userId provided.");
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateUserByUserId_ShouldThrowValidationException_WhenUserIdIsZero() {
        assertThatThrownBy(() -> appUserService.updateUserByUserId(0L, validUserDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid userId provided.");
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateUserByUserId_ShouldThrowValidationException_WhenUserIdIsNegative() {
        assertThatThrownBy(() -> appUserService.updateUserByUserId(-1L, validUserDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid userId provided.");
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateUserByUserId_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.updateUserByUserId(99L, validUserDTO))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(AppConstants.USER_NOT_FOUND + 99L);
    }

    @Test
    void updateUserByUserId_ShouldHandleGenericException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(userRepository.save(any(AppUser.class))).thenThrow(new RuntimeException("Save failed"));

        assertThatThrownBy(() -> appUserService.updateUserByUserId(1L, validUserDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(AppConstants.SOMETHING_WENT_WRONG);
    }
    
    // --- DeleteUserByUserId tests ---

    @Test
    void deleteUserByUserId_ShouldMarkUserInactive_WhenValidId() {
        validUser.setIsActive(true); // Ensure it's active initially for the test
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the modified user

        appUserService.DeleteUserByUserId(1L);

        // Capture the argument passed to save to verify its state
        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(userCaptor.capture());
        
        AppUser savedUser = userCaptor.getValue();
        assertThat(savedUser.getIsActive()).isFalse(); // Assert that isActive is set to false
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void deleteUserByUserId_ShouldThrowValidationException_WhenUserIdIsNull() {
        assertThatThrownBy(() -> appUserService.DeleteUserByUserId(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid userId provided.");
        verifyNoInteractions(userRepository);
    }

    @Test
    void deleteUserByUserId_ShouldThrowValidationException_WhenUserIdIsZero() {
        assertThatThrownBy(() -> appUserService.DeleteUserByUserId(0L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid userId provided.");
        verifyNoInteractions(userRepository);
    }

    @Test
    void deleteUserByUserId_ShouldThrowValidationException_WhenUserIdIsNegative() {
        assertThatThrownBy(() -> appUserService.DeleteUserByUserId(-1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid userId provided.");
        verifyNoInteractions(userRepository);
    }

    @Test
    void deleteUserByUserId_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.DeleteUserByUserId(55L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(AppConstants.USER_NOT_FOUND + 55L);
        verify(userRepository).findById(55L);
        verify(userRepository, never()).save(any(AppUser.class)); // Ensure save is not called
    }

    @Test
    void deleteUserByUserId_ShouldHandleGenericException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(userRepository.save(any(AppUser.class))).thenThrow(new RuntimeException("Delete operation failed"));

        assertThatThrownBy(() -> appUserService.DeleteUserByUserId(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(AppConstants.SOMETHING_WENT_WRONG);
    }

    // --- Helper mapping methods tests (implicitly covered by above, but can add explicit ones if needed) ---
    // For completeness, here's how you might test the private mapToEntity and mapToDTO methods if they were public
    // or if you wanted explicit coverage. For private methods, Mockito typically isn't used.
    // Instead, you'd test their logic by calling the public methods that use them.
    // The current tests effectively cover these mappings.

}