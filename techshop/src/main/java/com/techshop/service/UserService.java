package com.techshop.service;

import com.techshop.DTO.UserDTO;
import com.techshop.DTO.UserUpdateDTO;
import com.techshop.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    Optional<User> getUserByEmail(String email);
    User createUser(UserDTO userDTO);
    User updateUser(Long id, UserUpdateDTO userUpdateDTO);
    void deleteUser(Long id);
}
