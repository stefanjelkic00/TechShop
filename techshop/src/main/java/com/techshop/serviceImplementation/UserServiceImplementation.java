package com.techshop.serviceImplementation;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techshop.dto.RegisterUserDTO;
import com.techshop.dto.UserDTO;
import com.techshop.dto.UserUpdateDTO;
import com.techshop.enums.CustomerType;
import com.techshop.enums.Role;
import com.techshop.model.Cart;
import com.techshop.model.Order;
import com.techshop.model.User;
import com.techshop.repository.UserRepository;
import com.techshop.service.EmailService;
import com.techshop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // HashMap za privremeno čuvanje podataka o registraciji
    private final Map<String, RegisterUserDTO> pendingRegistrations = new HashMap<>();

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCustomerType()
                ));
    }

    @Override
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCustomerType()
                ));
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase());
    }

    @Override
    public void registerUser(RegisterUserDTO userDTO) {
        String email = userDTO.getEmail().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email je već u upotrebi.");
        }
        if (pendingRegistrations.values().stream().anyMatch(dto -> dto.getEmail().equalsIgnoreCase(email))) {
            throw new RuntimeException("Registracija je već u toku. Proverite vaš mejl za potvrdu.");
        }

        if (userDTO.getPassword().length() < 6) {
            throw new RuntimeException("Lozinka mora imati najmanje 6 karaktera.");
        }

        // Generisanje verifikacionog tokena
        String verificationToken = UUID.randomUUID().toString();

        // Čuvanje podataka u HashMap
        pendingRegistrations.put(verificationToken, userDTO);

        // Slanje verifikacionog mejla
        sendVerificationEmail(email, userDTO.getFirstName(), verificationToken);
    }

    @Override
    public void sendVerificationEmail(String toEmail, String firstName, String token) {
        emailService.sendVerificationEmail(toEmail, firstName, token);
    }

    @Override
    public void confirmRegistration(String token) {
        RegisterUserDTO userDTO = pendingRegistrations.get(token);
        if (userDTO == null) {
            throw new RuntimeException("Nevažeći token za verifikaciju.");
        }

        // Kreiramo korisnika u bazi
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(Role.CUSTOMER);

        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);

        userRepository.save(user);

        // Uklanjamo podatke iz HashMap-a nakon uspešne registracije
        pendingRegistrations.remove(token);
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
        return userRepository.existsByEmail(email.toLowerCase());
    }

    @Override
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            // Greška je već obrađena u AuthenticationFilter
            return;
        }

        String email = authentication.getName();
        User user = findUserByEmail(email)
                .orElseThrow(() -> {
                    // Proveravamo da li je email u pendingRegistrations
                    if (pendingRegistrations.values().stream().anyMatch(dto -> dto.getEmail().equalsIgnoreCase(email))) {
                        throw new RuntimeException("Registracija nije potvrđena. Proverite vaš mejl za potvrdu.");
                    }
                    throw new RuntimeException("User not found");
                });

        Algorithm algorithm = Algorithm.HMAC256("secretKey".getBytes());
        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + 15 * 60 * 1000; // 15 minuta
        String jwtToken = JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(expirationTime))
                .withIssuer(request.getRequestURI())
                .withClaim("roles", authentication.getAuthorities().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList()))
                .withClaim("firstName", user.getFirstName())
                .withClaim("lastName", user.getLastName())
                .withClaim("customerType", user.getCustomerType().name())
                .sign(algorithm);

        Map<String, Object> tokens = new HashMap<>();
        tokens.put("jwtToken", jwtToken);
        tokens.put("roles", authentication.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toList()));
        tokens.put("email", user.getEmail());
        tokens.put("firstName", user.getFirstName());
        tokens.put("lastName", user.getLastName());
        tokens.put("customerType", user.getCustomerType().name());

        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getOrders();
    }

    @Override
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Korisnik sa datim emailom ne postoji."));

        validatePasswordChange(user, currentPassword, newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response, String email) throws IOException {
        User dbUser = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Algorithm algorithm = Algorithm.HMAC256("secretKey".getBytes());
        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + 15 * 60 * 1000; // 15 minuta
        String newJwtToken = JWT.create()
                .withSubject(dbUser.getEmail())
                .withExpiresAt(new Date(expirationTime))
                .withIssuer(request.getRequestURI())
                .withClaim("roles", authentication.getAuthorities().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList()))
                .withClaim("firstName", dbUser.getFirstName())
                .withClaim("lastName", dbUser.getLastName())
                .withClaim("customerType", dbUser.getCustomerType().name())
                .sign(algorithm);

        Map<String, Object> tokens = new HashMap<>();
        tokens.put("jwtToken", newJwtToken);
        tokens.put("roles", authentication.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toList()));
        tokens.put("email", dbUser.getEmail());
        tokens.put("firstName", dbUser.getFirstName());
        tokens.put("lastName", dbUser.getLastName());
        tokens.put("customerType", dbUser.getCustomerType().name());

        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    @Override
    public void refreshTokenWithCustomerType(HttpServletRequest request, HttpServletResponse response, String email, CustomerType customerType) throws IOException {
        User dbUser = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Algorithm algorithm = Algorithm.HMAC256("secretKey".getBytes());
        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + 15 * 60 * 1000; // 15 minuta
        String newJwtToken = JWT.create()
                .withSubject(dbUser.getEmail())
                .withExpiresAt(new Date(expirationTime))
                .withIssuer(request.getRequestURI())
                .withClaim("roles", authentication.getAuthorities().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList()))
                .withClaim("firstName", dbUser.getFirstName())
                .withClaim("lastName", dbUser.getLastName())
                .withClaim("customerType", customerType.name())
                .sign(algorithm);

        Map<String, Object> tokens = new HashMap<>();
        tokens.put("jwtToken", newJwtToken);
        tokens.put("roles", authentication.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toList()));
        tokens.put("email", dbUser.getEmail());
        tokens.put("firstName", dbUser.getFirstName());
        tokens.put("lastName", dbUser.getLastName());
        tokens.put("customerType", customerType.name());

        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

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