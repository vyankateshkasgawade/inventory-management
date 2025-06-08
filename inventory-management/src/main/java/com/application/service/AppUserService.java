package com.application.service;



import java.util.List;

import com.application.dto.AppUserDTO;

public interface AppUserService 
{
    AppUserDTO registerUser(AppUserDTO userDTO);
    
    AppUserDTO getUserByUserId(Long userId);
    
    List<AppUserDTO> getAllUsers();
    
    AppUserDTO updateUserByUserId(Long userId, AppUserDTO userDTO);
    
   // void hardDeleteUserByUserId(Long userId);
    
    void DeleteUserByUserId(Long userId);
}
