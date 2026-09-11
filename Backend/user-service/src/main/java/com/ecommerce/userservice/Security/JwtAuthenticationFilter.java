package com.ecommerce.userservice.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private  final JwtService jwtService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //read authorization header
        String authReader = request.getHeader("Authorization");
        log.info("Authorization Header :"+authReader);

        //verify if header exits and start with bearer
        if(authReader==null || !authReader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        
        //extract jwt token 
        String jwt = authReader.substring(7);

        log.info(jwt);

        String email = jwtService.extractUsername(jwt);
        log.info("email from jwt token "+email);


        //Authenticate only if not already authenticated
        if(email!=null && SecurityContextHolder.getContext().getAuthentication()==null){
            UserDetails userDetail = userDetailsService.loadUserByUsername(email);

            if(jwtService.validateToken(jwt, userDetail)){

                userDetail.getAuthorities().forEach(auth->
                        log.info("Role:{}", auth.getAuthority())
                        );


                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetail,
                        null,
                        userDetail.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            
            
        }

        filterChain.doFilter(request, response);
    }
}
