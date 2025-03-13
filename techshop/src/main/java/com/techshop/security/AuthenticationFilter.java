package com.techshop.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techshop.repository.UserRepository;
import com.techshop.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository; //  Dodali smo UserRepository

    public AuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository; //  Inicijalizujemo UserRepository
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        System.out.println("🔹 Trying to authenticate user: " + email);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain,
                                            Authentication authentication) throws IOException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        //  Dohvatamo korisnika iz baze da bismo uzeli firstName, lastName i customerType
        User dbUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("✅ User authenticated successfully: " + dbUser.getEmail());

        Algorithm algorithm = Algorithm.HMAC256("secretKey".getBytes());

        //  Generišemo JWT token sa dodatnim podacima
        String jwtToken = JWT.create()
                .withSubject(dbUser.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                .withIssuer(request.getRequestURI())
                .withClaim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .withClaim("firstName", dbUser.getFirstName())  // 🚀 Dodajemo ime
                .withClaim("lastName", dbUser.getLastName())    // 🚀 Dodajemo prezime
                .withClaim("customerType", dbUser.getCustomerType().name()) // 🚀 Dodajemo customerType kao string
                .sign(algorithm);

        System.out.println("✅ JWT Token Generated: " + jwtToken);

        //  Vraćamo odgovor sa tokenom i korisničkim informacijama
        Map<String, Object> tokens = new HashMap<>();
        tokens.put("jwtToken", jwtToken);
        tokens.put("roles", userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList()));
        tokens.put("email", dbUser.getEmail());
        tokens.put("firstName", dbUser.getFirstName());
        tokens.put("lastName", dbUser.getLastName());
        tokens.put("customerType", dbUser.getCustomerType().name());

        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }
}
