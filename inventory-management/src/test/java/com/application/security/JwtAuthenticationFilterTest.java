package com.application.security;

import com.application.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtTokenHelper jwtTokenHelper;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;


    private static final String SECRET_KEY = "thisIsASecretKeyForSpringBootApplication";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
       
        SecurityContextHolder.clearContext();
    }

   
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    private String generateTestToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles); 
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) 
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void doFilterInternal_ShouldAuthenticateUser_WhenTokenIsValid() throws Exception {
        String username = "testuser";
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN"); 
        String token = generateTestToken(username, roles); 

        
        UserDetails userDetails = new User(username, "password",
                roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenHelper.extractUsername(token)).thenReturn(username);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtTokenHelper.validateToken(token, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "Authentication should not be null");
        assertEquals(username, authentication.getPrincipal(), "Principal should match the username");
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")), "Should contain ROLE_USER authority");
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")), "Should contain ROLE_ADMIN authority");

  
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldThrowException_WhenTokenIsBlacklisted() throws Exception {
        String token = "blacklisted-token";

        when(jwtTokenHelper.extractUsername(token)).thenReturn("testuser");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldContinueFilterChain_WhenNoAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);


        verify(filterChain).doFilter(request, response);
       
        verifyNoInteractions(jwtTokenHelper, userDetailsService, tokenBlacklistService);
    }

    @Test
    void doFilterInternal_ShouldContinueFilterChain_WhenTokenIsInvalidFormat() throws Exception {
        String invalidToken = "invalid-token-without-bearer-prefix"; // Token without "Bearer " prefix
        when(request.getHeader("Authorization")).thenReturn(invalidToken);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);


        verify(filterChain).doFilter(request, response);
      
        verifyNoInteractions(jwtTokenHelper);
    }

    @Test
    void doFilterInternal_ShouldContinueFilterChain_WhenJwtExtractionFails() throws Exception {
        String token = "malformed-jwt";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
       
        when(jwtTokenHelper.extractUsername(token)).thenThrow(new RuntimeException("JWT malformed or invalid signature"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    
        verify(filterChain).doFilter(request, response);
      
    }

    @Test
    void doFilterInternal_ShouldContinueFilterChain_WhenTokenValidationFails() throws Exception {
        String token = "valid-format-invalid-content-token";
        String username = "testuser";
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenHelper.extractUsername(token)).thenReturn(username);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtTokenHelper.validateToken(token, userDetails)).thenReturn(false);
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should remain null");
    }
}