package com.techshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techshop.dto.ChangePasswordDTO;
import com.techshop.dto.RegisterUserDTO;
import com.techshop.dto.UserDTO;
import com.techshop.dto.UserUpdateDTO;
import com.techshop.model.Order;
import com.techshop.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers()
                .stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCustomerType()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCustomerType()
                ))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCustomerType()
                ))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterUserDTO userDTO) {
        try {
            userService.registerUser(userDTO);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Registracija je započeta. Proverite vaš mejl za potvrdu.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Greška pri registraciji: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/verify")
    public void verifyUser(@RequestParam(value = "token", required = true) String token, HttpServletResponse response) throws IOException {
        logger.info("Primljen zahtev za verifikaciju sa tokenom: {}", token);
        try {
            if (token == null || token.trim().isEmpty()) {
                logger.warn("Token nije prosleđen ili je prazan");
                throw new IllegalArgumentException("No token provided");
            }
            userService.confirmRegistration(token);
            logger.info("Verifikacija uspešna za token: {}", token);

            // Preusmeravanje na login stranicu sa porukom
            String message = URLEncoder.encode("Registracija uspešno potvrđena! Sada se možete prijaviti.", StandardCharsets.UTF_8.toString());
            String redirectUrl = "http://localhost:3000/login?message=" + message;
            response.sendRedirect(redirectUrl);
        } catch (RuntimeException e) {
            logger.error("Greška pri verifikaciji: {}", e.getMessage());
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString());
            String redirectUrl = "http://localhost:3000/login?error=" + errorMessage;
            response.sendRedirect(redirectUrl);
        }
    }

    @PostMapping("/login")
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            logger.warn("Korisnik nije autentifikovan, vraćam 401 Unauthorized");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed: Invalid email or password");
            new ObjectMapper().writeValue(response.getOutputStream(), error);
            return;
        }

        userService.login(request, response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO userUpdateDTO) {
        try {
            userService.updateUser(id, userUpdateDTO);
            return ResponseEntity.ok("User updated successfully.");
        } catch (RuntimeException e) {
            logger.error("Greška pri ažuriranju korisnika: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully.");
        } catch (RuntimeException e) {
            logger.error("Greška pri brisanju korisnika: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long id) {
        try {
            List<Order> orders = userService.getUserOrders(id);
            return ResponseEntity.ok(orders);
        } catch (RuntimeException e) {
            logger.error("Greška pri dohvatanju porudžbina korisnika: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDTO passwordDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Change password request with authentication: {}", authentication);

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            logger.warn("Korisnik nije autentifikovan, vraćam 401 Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            String email = authentication.getName();
            userService.changePassword(email, passwordDTO.getCurrentPassword(), passwordDTO.getNewPassword());
            return ResponseEntity.ok("Lozinka uspešno promenjena.");
        } catch (IllegalArgumentException e) {
            logger.error("Greška pri promeni lozinke: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    @PreAuthorize("isAuthenticated()")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Refresh token request with authentication: {}", authentication);

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            logger.warn("Korisnik nije autentifikovan, vraćam 401 Unauthorized");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not authenticated");
            new ObjectMapper().writeValue(response.getOutputStream(), error);
            return;
        }

        String email = authentication.getName();
        userService.refreshToken(request, response, email);
    }
}