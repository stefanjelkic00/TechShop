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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String requestUri = request.getRequestURI();
        System.out.println("🔹 Processing request - ServletPath: " + servletPath + ", RequestURI: " + requestUri);

        // Izuzeci za javne rute
        if (servletPath.startsWith("/api/users/login") || 
            servletPath.startsWith("/api/users/register") || 
            servletPath.startsWith("/api/elasticsearch/")) {
            System.out.println("🔹 Skipping authorization for public route: " + servletPath);
            filterChain.doFilter(request, response); // Preskači filter za ove rute
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String jwtToken = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("secretKey".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(jwtToken);

                String email = decodedJWT.getSubject();
                String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
                String firstName = decodedJWT.getClaim("firstName").asString();
                String lastName = decodedJWT.getClaim("lastName").asString();
                String customerType = decodedJWT.getClaim("customerType").asString();

                System.out.println("🔹 Extracted user info from JWT - Email: " + email + ", Roles: " + java.util.Arrays.toString(roles));

                java.util.Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority(role));
                }

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                filterChain.doFilter(request, response);
            } catch (Exception exception) {
                System.out.println("❌ JWT validation failed: " + exception.getMessage());
                response.setHeader("error", exception.getMessage());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                Map<String, String> error = new HashMap<>();
                error.put("error", exception.getMessage());
                response.setContentType("application/json");
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        } else {
            System.out.println("🔹 No token provided for path: " + servletPath + " - proceeding to next filter");
            filterChain.doFilter(request, response); // Ako nema tokena, nastavi za javne rute
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        boolean shouldSkip = path.startsWith("/api/users/login") ||
                            path.startsWith("/api/users/register") ||
                            path.startsWith("/api/elasticsearch/");
        if (shouldSkip) {
            System.out.println("🔹 shouldNotFilter: Skipping filter for path: " + path);
        }
        return shouldSkip;
    }
}