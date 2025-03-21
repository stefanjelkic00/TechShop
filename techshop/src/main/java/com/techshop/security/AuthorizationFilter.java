package com.techshop.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        System.out.println("🔹 Processing request - ServletPath: " + servletPath);

        // Dodajem sve javne rute definisane u SecurityConfig
        if (servletPath.startsWith("/api/users/login") || 
            servletPath.startsWith("/api/users/register") || 
            servletPath.startsWith("/api/users/verify") || 
            servletPath.startsWith("/api/elasticsearch/") ||
            servletPath.startsWith("/api/products/") ||  // Dodajemo /api/products/**
            servletPath.startsWith("/api/categories/") ||  // Dodajemo /api/categories/**
            servletPath.startsWith("/api/sync/")) {  // Dodajemo /api/sync/**
            System.out.println("🔹 Skipping authorization for public route: " + servletPath);
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            System.out.println("❌ No valid Authorization header for protected route: " + servletPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Map<String, String> error = new HashMap<>();
            error.put("error", "No token provided");
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getOutputStream(), error);
            return;
        }

        try {
            String jwtToken = authorizationHeader.substring("Bearer ".length());
            System.out.println("🔹 Validating token: " + jwtToken);
            Algorithm algorithm = Algorithm.HMAC256("secretKey".getBytes());
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(jwtToken);

            if (decodedJWT.getExpiresAt().before(new Date())) {
                System.out.println("❌ Token has expired for path: " + servletPath);
                throw new Exception("Token has expired");
            }

            String email = decodedJWT.getSubject();
            String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
            System.out.println("🔹 Decoded JWT - Email: " + email + ", Roles: " + java.util.Arrays.toString(roles));

            if (roles == null || roles.length == 0) {
                System.out.println("❌ No roles found in token for user: " + email);
                throw new Exception("No roles found in token");
            }

            java.util.Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for (String role : roles) {
                String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                authorities.add(new SimpleGrantedAuthority(authority));
                System.out.println("🔹 Added authority: " + authority);
            }

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(context);

            System.out.println("🔹 Authentication set for: " + email + ", Authorities: " + authorities);
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            System.out.println("❌ Error processing request: " + exception.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Forbidden: " + exception.getMessage().replaceAll("[\\r\\n]", ""));
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getOutputStream(), error);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // Dodajem sve javne rute u uslov za preskakanje
        boolean shouldSkip = path.startsWith("/api/users/login") ||
                            path.startsWith("/api/users/register") ||
                            path.startsWith("/api/users/verify") ||
                            path.startsWith("/api/elasticsearch/") ||
                            path.startsWith("/api/products/") ||  // Dodajem /api/products/**
                            path.startsWith("/api/categories/") ||  // Dodajem /api/categories/**
                            path.startsWith("/api/sync/");  // Dodajem /api/sync/**
        if (shouldSkip) {
            System.out.println("🔹 shouldNotFilter: Skipping filter for path: " + path);
        }
        return shouldSkip;
    }
}