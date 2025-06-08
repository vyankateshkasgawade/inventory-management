package com.application.repository;
import com.application.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
   Optional<AppUser> findByName(String name); 
    Optional<AppUser> findByPhone(String phone); 
    Optional<AppUser> findByEmail(String email); 
    
}