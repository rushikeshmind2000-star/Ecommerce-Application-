package com.ecommerce.productservice.interceptor;

import com.ecommerce.productservice.annotation.RequireRole;
import com.ecommerce.productservice.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class RoleAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // Check if method or class has @RequireRole
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        // If no annotation, endpoint is public
        if (requireRole == null) {
            return true;
        }

        // Enforce Authentication: require X-User-Role header OR valid JWT token
        String userRole = request.getHeader("X-User-Role");
        
        if (userRole == null || userRole.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.isTokenValid(token)) {
                    userRole = jwtUtil.extractRole(token);
                }
            }
        }

        if (userRole == null || userRole.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Unauthorized: Missing X-User-Role header or Invalid/Missing JWT Token.");
            return false;
        }

        // Enforce Authorization: check if userRole matches required roles
        boolean isAuthorized = Arrays.asList(requireRole.value()).contains(userRole);
        if (!isAuthorized) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Forbidden: You do not have the required role to access this endpoint.");
            return false;
        }

        return true;
    }
}
