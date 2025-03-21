package com.techshop.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techshop.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public AuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        setFilterProcessesUrl("/api/users/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // IZMENA: Vraćamo na form-data format umesto JSON-a
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        System.out.println("🔹 Trying to authenticate user: " + email);

        if (email == null || password == null) {
            throw new AuthenticationException("Email or password missing") {};
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authentication) throws IOException, ServletException {
        // IZMENA: Uklanjamo suvišnu proveru korisnika jer je već obavljena u CustomUserDetailsService
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("✅ User authenticated successfully: " + authentication.getName());
        chain.doFilter(request, response); // Prosledi kontrolu dalje (UserController)
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        System.out.println("❌ Authentication failed: " + failed.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid email or password");
        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}