package com.ecommerce.userservice.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter.
 * Verifies that the filter correctly sets the SecurityContext for:
 *   - gateway-forwarded requests (X-User-Role header)
 *   - direct requests with a valid Bearer JWT
 *   - direct requests with an invalid Bearer JWT
 *   - requests with no auth at all
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanUpContext() {
        SecurityContextHolder.clearContext();
    }

    // ─── Gateway header path ──────────────────────────────────────────────────

    @Test
    @DisplayName("Sets authentication from X-User-Role / X-User-Id headers (gateway path)")
    void doFilter_gatewayHeaders_setsAuthentication() throws ServletException, IOException {
        when(request.getHeader("X-User-Role")).thenReturn("ADMIN");
        when(request.getHeader("X-User-Id")).thenReturn(UUID.randomUUID().toString());

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Filter chain must still be called
        verify(filterChain).doFilter(request, response);
        // JwtUtil should NOT be used when gateway headers are present
        verify(jwtUtil, never()).isTokenValid(anyString());
    }

    // ─── Valid JWT Bearer path ────────────────────────────────────────────────

    @Test
    @DisplayName("Sets authentication from valid Bearer JWT in Authorization header")
    void doFilter_validBearerToken_setsAuthentication() throws ServletException, IOException {
        String fakeToken = "valid.jwt.token";
        String userId    = UUID.randomUUID().toString();

        when(request.getHeader("X-User-Role")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + fakeToken);
        when(jwtUtil.isTokenValid(fakeToken)).thenReturn(true);
        when(jwtUtil.extractRole(fakeToken)).thenReturn("USER");
        when(jwtUtil.extractUserId(fakeToken)).thenReturn(userId);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(userId);
        assertThat(auth.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        verify(filterChain).doFilter(request, response);
    }

    // ─── Invalid JWT Bearer path ──────────────────────────────────────────────

    @Test
    @DisplayName("Does not set authentication for an invalid Bearer JWT")
    void doFilter_invalidBearerToken_doesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("X-User-Role")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
        when(jwtUtil.isTokenValid("invalid.token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        // Chain must still continue so Spring Security can reject the request
        verify(filterChain).doFilter(request, response);
    }

    // ─── No auth header path ──────────────────────────────────────────────────

    @Test
    @DisplayName("Continues filter chain without setting auth when Authorization header is absent")
    void doFilter_noAuthHeader_continuesChain() throws ServletException, IOException {
        when(request.getHeader("X-User-Role")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).isTokenValid(anyString());
    }

    @Test
    @DisplayName("Continues filter chain when Authorization header lacks 'Bearer ' prefix")
    void doFilter_malformedAuthHeader_continuesChain() throws ServletException, IOException {
        when(request.getHeader("X-User-Role")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).isTokenValid(anyString());
    }
}
