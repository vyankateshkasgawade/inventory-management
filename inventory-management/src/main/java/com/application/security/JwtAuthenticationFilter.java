//package com.application.security;
//
//import java.io.IOException;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private JwtTokenHelper jwtTokenHelper;
//
//    @Autowired
//    private CustomUserDetailsService userDetailsService;
//
//    @Autowired
//    private TokenBlacklistService tokenBlacklistService;  // inject TokenBlacklistService
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String requestToken = request.getHeader("Authorization");
//
//        String username = null;
//        String token = null;
//
//        if (requestToken != null && requestToken.startsWith("Bearer ")) {
//            token = requestToken.substring(7);
//            try {
//                username = jwtTokenHelper.extractUsername(token);
//            } catch (Exception e) {
//                System.out.println("Invalid JWT token");
//            }
//        }
//
//        if (token != null && tokenBlacklistService.isBlacklisted(token)) {
//            throw new com.application.exception.InvalidTokenException("Token has been invalidated. Please login again.");
//        }
//
//        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null)
//        {
//            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//            
//            if (jwtTokenHelper.validateToken(token, userDetails)) 
//            {
//                UsernamePasswordAuthenticationToken authenticationToken =
//                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//}

package com.application.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    private static final String SECRET_KEY = "thisIsASecretKeyForSpringBootApplication";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestToken = request.getHeader("Authorization");

        String username = null;
        String token = null;

        if (requestToken != null && requestToken.startsWith("Bearer ")) {
            token = requestToken.substring(7);
            try {
                username = jwtTokenHelper.extractUsername(token);
            } catch (Exception e) {
                System.out.println("Invalid JWT token");
            }
        }

        if (token != null && tokenBlacklistService.isBlacklisted(token)) {
            throw new com.application.exception.InvalidTokenException("Token has been invalidated. Please login again.");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Validate the token with userDetailsService to check user validity
            if (jwtTokenHelper.validateToken(token, userDetailsService.loadUserByUsername(username))) {

                // Extract roles from the JWT token claims
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                List<String> roles = claims.get("roles", List.class);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }

}

