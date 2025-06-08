package com.application.config;

import com.application.security.CustomUserDetailsService;
import com.application.security.JwtAuthenticationEntryPoint;
import com.application.security.JwtAuthenticationFilter;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

   // private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                           JwtAuthenticationEntryPoint authenticationEntryPoint,
                           JwtAuthenticationFilter jwtAuthenticationFilter) 
    {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        log.info("Security filter chain");

        http
            .cors() // ✅ Enable CORS
            .and()
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
            		// .requestMatchers("/**").permitAll()
            		 .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/api/auth/**", "/login", "/logout").permitAll()
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/api/wishlist").permitAll()
                .requestMatchers("/api/auth/**").permitAll() // Allow all auth endpoints (login, forgot-password, verify-otp, send-registration-otp)
                .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/cart/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/categories/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/products/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/purchases/**").hasAnyRole("ADMIN", "USER")

                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Before build in security chain");
        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
    	
    	log.info("BCrypt password encoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    	
    	log.info("Authentication manager ");
        return config.getAuthenticationManager();
    }

	
}
