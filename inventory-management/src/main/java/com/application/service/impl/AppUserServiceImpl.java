package com.application.service.impl;
import com.application.dto.AppUserDTO;
import com.application.dto.AddressDTO; // Import AddressDTO
import com.application.entity.AppUser;
import com.application.entity.Address; // Import Address entity
import com.application.exception.UserAlreadyExistsException;
import com.application.exception.UserNotFoundException;
import com.application.exception.ValidationException;
import com.application.repository.AppUserRepository;
import com.application.repository.AddressRepository; // Assuming you have an AddressRepository
import com.application.service.AppUserService;
import com.application.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository; // Inject AddressRepository

    @Override
    public AppUserDTO registerUser(AppUserDTO userDTO) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Registering new user: {}", userDTO.getEmail());

        try {
        	
        	// Validation check for null fields: user name, email, and phone must not be null.
            if (userDTO.getName() == null || userDTO.getEmail() == null || userDTO.getPhone() == null) {
                throw new ValidationException("Username, email, and phone must not be null.");
            }

            Optional<AppUser> existingUserByEmail = userRepository.findByEmail(userDTO.getEmail());
            if (existingUserByEmail.isPresent()) 
            {
            	 // The UserAlreadyExistsException is thrown if a user already exists with the given email address.
                throw new UserAlreadyExistsException("User with email '" + userDTO.getEmail() + "' already exists.");
            }

            Optional<AppUser> existingUserByPhone = userRepository.findByPhone(userDTO.getPhone());
            if (existingUserByPhone.isPresent())
            {
            	// The UserAlreadyExistsException is thrown if a user already exists with the given phone number.
                throw new UserAlreadyExistsException("User with phone number '" + userDTO.getPhone() + "' already exists.");
            }
            if (userDTO.getRole() == null || userDTO.getRole().isEmpty()) {
                userDTO.setRole("USER"); // ✅ Set default role if not provided
            }

            System.out.println(" DTO user password before store or encode "+ userDTO.getPassword());
            AppUser user = mapToEntity(userDTO);
            System.out.println("Entity user password before store or encode "+ user.getPassword());
            user.setIsActive(true);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // Save address if present
            if (user.getAddress() != null) {
                user.getAddress().setUser(user); // Set the back-reference
            }
            
            AppUser savedUser = userRepository.save(user);
            System.out.println(savedUser);
            return mapToDTO(savedUser);

        } catch (ValidationException | UserAlreadyExistsException e) 
        {
            log.warn("Validation or duplicate error: {}", e.getMessage());
            throw e;
        } catch (DataIntegrityViolationException e) 
        {
            log.error("Database constraint violation: {}", e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
            
        } catch (Exception e) 
        {
            log.error("Unexpected error during user registration: {}", userDTO.getEmail(), e);
            throw new RuntimeException(AppConstants.SOMETHING_WENT_WRONG + " " + e.getMessage(), e);
        }
    }

    
	@Override
    public AppUserDTO getUserByUserId(Long userId) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Fetching user by ID: {}", userId);

        try {
            if (userId == null || userId <= 0) {
                throw new ValidationException("Invalid userId provided.");
            }
            	
            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND + userId));
            if(!user.getIsActive()) {
            	throw new UserNotFoundException(AppConstants.USER_NOT_FOUND + userId);
            }
            return mapToDTO(user);

        } catch (ValidationException | UserNotFoundException e)
        {
            log.warn("Validation/User not found error: {}", e.getMessage());
            throw e;
        } catch (Exception e) 
        {
            log.error("Unexpected error while fetching user by ID: {}", userId, e);
            throw new RuntimeException(AppConstants.SOMETHING_WENT_WRONG + " " + e.getMessage(), e);
        }
    }

    @Override
    public List<AppUserDTO> getAllUsers()
    {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Fetching all users");

        try {
            List<AppUser> users = userRepository.findAll();
            return users.stream().filter(u -> u.getIsActive()==true).map(this::mapToDTO).collect(Collectors.toList());

        } catch (RuntimeException e) 
        {
            log.warn("Runtime exception during fetchAll: {}", e.getMessage());
            throw e;
        } //catch (Exception e)
//        {
//            log.error("Unexpected error while fetching all users", e);
//            throw new RuntimeException(AppConstants.SOMETHING_WENT_WRONG + " " + e.getMessage(), e);
//        }
    }

    @Override
    public AppUserDTO updateUserByUserId(Long userId, AppUserDTO userDTO)
    {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Updating user by ID: {}", userId);

        try {
            if (userId == null || userId <= 0)
            {
                throw new ValidationException("Invalid userId provided.");
            }

            AppUser existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND + userId));

            existingUser.setName(userDTO.getName());
            existingUser.setEmail(userDTO.getEmail());
            existingUser.setDob(userDTO.getDob());
            existingUser.setPhone(userDTO.getPhone());
            
            // Update address details if provided
            if (userDTO.getAddress() != null) {
                Address existingAddress = existingUser.getAddress();
                if (existingAddress == null) {
                    existingAddress = new Address();
                    existingAddress.setUser(existingUser); // Set the back-reference
                }
                AddressDTO addressDTO = userDTO.getAddress();
                existingAddress.setCountryRegion(addressDTO.getCountryRegion());
                existingAddress.setPincode(addressDTO.getPincode());
                existingAddress.setFlatHouseNoBuildingCompanyApartment(addressDTO.getFlatHouseNoBuildingCompanyApartment());
                existingAddress.setAreaStreetSectorVillage(addressDTO.getAreaStreetSectorVillage());
                existingAddress.setLandmark(addressDTO.getLandmark());
                existingAddress.setTownCity(addressDTO.getTownCity());
                existingAddress.setState(addressDTO.getState());
                existingUser.setAddress(existingAddress);
            } else if (existingUser.getAddress() != null) {
                // If address DTO is null but existing user has an address, you might want to delete it or handle it as per business logic
                addressRepository.delete(existingUser.getAddress());
                existingUser.setAddress(null);
            }
            
            AppUser updatedUser = userRepository.save(existingUser);
            return mapToDTO(updatedUser);

        } catch (ValidationException | UserNotFoundException e)
        {
            log.warn("Validation/User not found error during update: {}", e.getMessage());
            throw e;
        } catch (Exception e)
        {
            log.error("Unexpected error updating user with ID: {}", userId, e);
            throw new RuntimeException(AppConstants.SOMETHING_WENT_WRONG + " " + e.getMessage(), e);
        }
    }

    
    
    @Override
    public void DeleteUserByUserId(Long userId)
    {
        log.info(AppConstants.SERVICE_LOG_PREFIX + " Deleting user by ID: {}", userId);

        try {
            if (userId == null || userId <= 0) 
            {
                throw new ValidationException("Invalid userId provided.");
            }

            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND + userId));
           user.setIsActive(false);
           userRepository.save(user);

        } catch (ValidationException | UserNotFoundException e)
        {
            log.warn("Validation/User not found error during delete: {}", e.getMessage());
            throw e;
        } catch (Exception e)
        {
            log.error("Unexpected error deleting user with ID: {}", userId, e);
            throw new RuntimeException(AppConstants.SOMETHING_WENT_WRONG + " " + e.getMessage(), e);
        }
    }    
    
    // Mapping method from DTO to Entity
    private AppUser mapToEntity(AppUserDTO dto) {
        AppUser user = AppUser.builder()
                .userId(dto.getUserId())
                .name(dto.getName())
                .email(dto.getEmail())
                .dob(dto.getDob())
                .phone(dto.getPhone())
                .password(dto.getPassword())
                .role(dto.getRole())
                .isActive(dto.getIsActive()) // Map isActive for completeness
                .build();

        if (dto.getAddress() != null) {
            Address address = Address.builder()
                    .countryRegion(dto.getAddress().getCountryRegion())
                    .pincode(dto.getAddress().getPincode())
                    .flatHouseNoBuildingCompanyApartment(dto.getAddress().getFlatHouseNoBuildingCompanyApartment())
                    .areaStreetSectorVillage(dto.getAddress().getAreaStreetSectorVillage())
                    .landmark(dto.getAddress().getLandmark())
                    .townCity(dto.getAddress().getTownCity())
                    .state(dto.getAddress().getState())
                    .build();
            address.setUser(user); // Set the back-reference
            user.setAddress(address);
        }
        return user;
    }

    // Mapping method from Entity to DTO
    private AppUserDTO mapToDTO(AppUser entity) {
        AppUserDTO dto = AppUserDTO.builder()
                .userId(entity.getUserId())
                .name(entity.getName())
                .email(entity.getEmail())
                .dob(entity.getDob())
                .phone(entity.getPhone())
                .password(entity.getPassword())
                .isActive(entity.getIsActive())
                .role(entity.getRole())
                .build();

        if (entity.getAddress() != null) {
            AddressDTO addressDTO = AddressDTO.builder()
                    .countryRegion(entity.getAddress().getCountryRegion())
                    .pincode(entity.getAddress().getPincode())
                    .flatHouseNoBuildingCompanyApartment(entity.getAddress().getFlatHouseNoBuildingCompanyApartment())
                    .areaStreetSectorVillage(entity.getAddress().getAreaStreetSectorVillage())
                    .landmark(entity.getAddress().getLandmark())
                    .townCity(entity.getAddress().getTownCity())
                    .state(entity.getAddress().getState())
                    .build();
            dto.setAddress(addressDTO);
        }
        return dto;
    }
}