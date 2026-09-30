package com.ecommerce.userservice.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. If request comes from Gateway, it might have X-User-Role header
        String gatewayRole = request.getHeader("X-User-Role");
        String gatewayId = request.getHeader("X-User-Id");
        
        if (gatewayRole != null) {
            setAuthenticationContext(gatewayId, gatewayRole);
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Direct calls (like from Swagger UI on port 8081)
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.isTokenValid(token)) {
                String role = jwtUtil.extractRole(token);
                String userId = jwtUtil.extractUserId(token);
                setAuthenticationContext(userId, role);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthenticationContext(String userId, String role) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, List.of(authority));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
