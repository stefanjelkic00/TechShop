package com.techshop.service;

import com.techshop.dto.RegisterUserDTO;
import com.techshop.dto.UserDTO;
import com.techshop.dto.UserUpdateDTO;
import com.techshop.enums.CustomerType;
import com.techshop.model.Order;
import com.techshop.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();
    Optional<UserDTO> getUserById(Long id);
    Optional<UserDTO> getUserByEmail(String email);
    Optional<User> findUserByEmail(String email);
    void registerUser(RegisterUserDTO userDTO); // Menjamo createUser u registerUser
    User updateUser(Long id, UserUpdateDTO userUpdateDTO);
    void deleteUser(Long id);
    boolean existsByEmail(String email);
    void login(HttpServletRequest request, HttpServletResponse response) throws IOException;
    List<Order> getUserOrders(Long userId);
    void changePassword(String email, String currentPassword, String newPassword);
    void refreshToken(HttpServletRequest request, HttpServletResponse response, String email) throws IOException;
    void refreshTokenWithCustomerType(HttpServletRequest request, HttpServletResponse response, String email, CustomerType customerType) throws IOException;
    
    // Nova metoda za slanje verifikacionog mejla
    void sendVerificationEmail(String toEmail, String firstName, String token);
    
    // Nova metoda za potvrdu registracije
    void confirmRegistration(String token);
}