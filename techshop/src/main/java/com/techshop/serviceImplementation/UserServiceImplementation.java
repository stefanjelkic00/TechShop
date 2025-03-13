package com.techshop.serviceImplementation;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techshop.dto.RegisterUserDTO;
import com.techshop.dto.UserUpdateDTO;
import com.techshop.enums.Role;
import com.techshop.model.Cart;
import com.techshop.model.Order;
import com.techshop.model.User;
import com.techshop.repository.UserRepository;
import com.techshop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            System.out.println("User found in database: " + user.get().getEmail() + " | Role: " + user.get().getRole());
        } else {
            System.out.println("User NOT FOUND in database!");
        }
        return user;
    }

    @Override
    public User createUser(RegisterUserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email is already in use.");
        }

        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());

        if (userDTO.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long.");
        }

        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(Role.CUSTOMER);

        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);

        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setFirstName(userUpdateDTO.getFirstName());
            user.setLastName(userUpdateDTO.getLastName());
            user.setEmail(userUpdateDTO.getEmail());
            user.setCustomerType(userUpdateDTO.getCustomerType());

            if (user.getRole() != Role.ADMIN) {
                user.setRole(userUpdateDTO.getRole());
            }

            if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
            }

            return userRepository.save(user);
        }
        throw new RuntimeException("User not found");
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            UsernamePasswordAuthenticationToken authenticationToken = 
                    new UsernamePasswordAuthenticationToken(email, password);
            
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            UserDetails user = (UserDetails) authentication.getPrincipal();

            Algorithm algorithm = Algorithm.HMAC256("secretKey".getBytes());

            String jwtToken = JWT.create()
                    .withSubject(user.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                    .withIssuer(request.getRequestURI())
                    .withClaim("role", user.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                    .sign(algorithm);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("jwtToken", jwtToken);
            tokens.put("role", user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).collect(Collectors.toList()).get(0));
            tokens.put("email", user.getUsername());

            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getOutputStream(), tokens);
        } catch (AuthenticationException e) {
            // Rukovanje greškom ako autentifikacija ne uspe
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid email or password");
            new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
        } catch (Exception e) {
            // Rukovanje ostalim greškama
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred");
            new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
        }
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getOrders();
    }
    
    
    //logika za izmenu lozinke
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Korisnik sa datim emailom ne postoji."));

        validatePasswordChange(user, currentPassword, newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    //logika za izmenu lozinke
    private void validatePasswordChange(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Trenutna lozinka nije ispravna.");
        }
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("Nova lozinka ne može biti ista kao trenutna.");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Nova lozinka mora imati najmanje 6 karaktera.");
        }
    }

    
    
}