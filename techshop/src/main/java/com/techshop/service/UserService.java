package com.techshop.service;

import com.techshop.dto.UserUpdateDTO;
import com.techshop.model.Order;
import com.techshop.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    Optional<User> getUserByEmail(String email);

    boolean existsByEmail(String email);

    User createUser(UserUpdateDTO userDTO);

    User updateUser(Long id, UserUpdateDTO userUpdateDTO);

    void deleteUser(Long id);
    
    void login(HttpServletRequest request, HttpServletResponse response) throws IOException;
    
    List<Order> getUserOrders(Long userId);


}
