package com.application.dto;



import org.junit.jupiter.api.Test;

import com.application.dto.AppUserDTO;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class AppUserDTOTest {

    @Test
    public void testAppUserDTO() {
        // Create test data
        LocalDate dob = LocalDate.of(1990, 5, 15);

        // Create and test AppUserDTO
        AppUserDTO userDTO = AppUserDTO.builder()
                .userId(1L)
                .name("John Doe")
                .email("john@example.com")
                .dob("1990/5/15")
                .phone("1234567890")
                .password("password123")
                .isActive(true)
                .role("USER")
                .build();

    }
}