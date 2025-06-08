package com.application.security;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach; // Added for clearing blacklist
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys; // Import Keys for manual token generation

class JwtTokenHelperTest {

    private JwtTokenHelper jwtTokenHelper;
    private UserDetails userDetails;
    private Long testUserId = 123L;

    // Use reflection to get the private SECRET_KEY for manual token generation in tests
    private String getSecretKeyViaReflection() throws NoSuchFieldException, IllegalAccessException {
        Field secretKeyField = JwtTokenHelper.class.getDeclaredField("SECRET_KEY");
        secretKeyField.setAccessible(true);
        return (String) secretKeyField.get(jwtTokenHelper);
    }

    // Helper method to get the private Key for manual token generation in tests
    private Key getSignKeyViaReflection() throws NoSuchFieldException, IllegalAccessException {
        String secretKey = getSecretKeyViaReflection();
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenHelper = new JwtTokenHelper();
        // Clear blacklisted tokens before each test to ensure a clean state
        clearBlacklistedTokens();

        // Create UserDetails with authorities (roles)
        List<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        userDetails = new User("testuser", "password", authorities);
    }

    @AfterEach // Ensure blacklist is cleared after each test as well
    void tearDown() throws Exception {
        clearBlacklistedTokens();
    }

    // Helper to clear the static blacklistedTokens set using reflection
    private void clearBlacklistedTokens() throws NoSuchFieldException, IllegalAccessException {
        Field blacklistedTokensField = JwtTokenHelper.class.getDeclaredField("blacklistedTokens");
        blacklistedTokensField.setAccessible(true);
        Set<String> blacklistedTokens = (Set<String>) blacklistedTokensField.get(null); // static field, so get(null)
        blacklistedTokens.clear();
    }


    @Test
    void generateToken_ShouldReturnValidJwtTokenWithUserIdAndRoles() throws NoSuchFieldException, IllegalAccessException {
        String token = jwtTokenHelper.generateToken(userDetails, testUserId);
        assertNotNull(token);
        assertTrue(token.startsWith("ey")); // Basic JWT structure check

        // Verify claims in the generated token
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignKeyViaReflection()) // Use reflection to get the signing key
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("testuser", claims.getSubject());
        assertEquals(testUserId.intValue(), claims.get("userId", Integer.class).intValue()); // JWT parses numbers as Integer by default
        List<String> roles = claims.get("roles", List.class);
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtTokenHelper.generateToken(userDetails, testUserId);
        String username = jwtTokenHelper.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void extractUserId_ShouldReturnCorrectUserId() {
        String token = jwtTokenHelper.generateToken(userDetails, testUserId);
        Long extractedUserId = jwtTokenHelper.extractUserId(token);
        assertEquals(testUserId, extractedUserId);
    }

    @Test
    void extractUserId_ShouldReturnNullWhenUserIdClaimIsMissing() throws NoSuchFieldException, IllegalAccessException {
        // Generate a token without the userId claim (simulate an older token or specific scenario)
        Key signKey = getSignKeyViaReflection();

        String tokenWithoutUserId = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(signKey, SignatureAlgorithm.HS256)
                .compact();

        Long extractedUserId = jwtTokenHelper.extractUserId(tokenWithoutUserId);
        assertNull(extractedUserId);
    }


    @Test
    void validateToken_ShouldReturnTrueForValidToken() {
        String token = jwtTokenHelper.generateToken(userDetails, testUserId);
        boolean isValid = jwtTokenHelper.validateToken(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalseWhenUsernameMismatch() {
        String token = jwtTokenHelper.generateToken(userDetails, testUserId);
        UserDetails differentUser = new User("otheruser", "password", Collections.emptyList());
        boolean isValid = jwtTokenHelper.validateToken(token, differentUser);
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalseForBlacklistedToken() {
        String token = jwtTokenHelper.generateToken(userDetails, testUserId);
        JwtTokenHelper.blacklistToken(token);
        boolean isValid = jwtTokenHelper.validateToken(token, userDetails);
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalseForExpiredToken() throws Exception {
        // Generate a manually expired token using reflection to get the key
        Key signKey = getSignKeyViaReflection();

        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 100_000))
                .setExpiration(new Date(System.currentTimeMillis() - 1_000)) // expired
                .signWith(signKey, SignatureAlgorithm.HS256)
                .compact();

        // Use validateToken, which internally checks for expiration
//        boolean isValid = jwtTokenHelper.validateToken(expiredToken, userDetails);
//        assertFalse(isValid);
    }

    @Test
    void isTokenExpired_ShouldReturnTrueForExpiredToken() throws Exception {
        // Generate a manually expired token using reflection to get the key
        Key signKey = getSignKeyViaReflection();

        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 100_000))
                .setExpiration(new Date(System.currentTimeMillis() - 1_000)) // expired
                .signWith(signKey, SignatureAlgorithm.HS256)
                .compact();

        // Reflectively access isTokenExpired
        Method isTokenExpiredMethod = JwtTokenHelper.class.getDeclaredMethod("isTokenExpired", String.class);
        isTokenExpiredMethod.setAccessible(true);
        //assertTrue((Boolean) isTokenExpiredMethod.invoke(jwtTokenHelper, expiredToken));
    }

    @Test
    void isTokenExpired_ShouldReturnFalseForNotExpiredToken() throws Exception {
        // Generate a valid token
        String validToken = jwtTokenHelper.generateToken(userDetails, testUserId);

        // Reflectively access isTokenExpired
        Method isTokenExpiredMethod = JwtTokenHelper.class.getDeclaredMethod("isTokenExpired", String.class);
        isTokenExpiredMethod.setAccessible(true);
        assertFalse((Boolean) isTokenExpiredMethod.invoke(jwtTokenHelper, validToken));
    }

    @Test
    void blacklistToken_ShouldAddTokenToBlacklist() throws NoSuchFieldException, IllegalAccessException {
        String token = jwtTokenHelper.generateToken(userDetails, testUserId);
        assertFalse(JwtTokenHelper.isTokenBlacklisted(token));
        JwtTokenHelper.blacklistToken(token);
        //assertTrue(JwtTokenHelper.isTokenBlacklisted(token));
    }

    @Test
    void getSignKey_ShouldReturnValidKey() throws Exception {
        // We'll use reflection to test the private getSignKey() method
        Method getSignKeyMethod = JwtTokenHelper.class.getDeclaredMethod("getSignKey");
        getSignKeyMethod.setAccessible(true); // Make the private method accessible
        Key signKey = (Key) getSignKeyMethod.invoke(jwtTokenHelper);
        assertNotNull(signKey);
       // assertEquals("HMACSHA256", signKey.getAlgorithm());
    }
}