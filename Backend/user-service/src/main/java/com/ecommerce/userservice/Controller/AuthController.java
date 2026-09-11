package com.ecommerce.userservice.Controller;


import com.TechPulse.LearnSpringSecurity.DTO.LoginReq;
import com.TechPulse.LearnSpringSecurity.DTO.LoginResponce;
import com.TechPulse.LearnSpringSecurity.Security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {


     private final JwtService jwtService;
     private final AuthenticationManager authenticationManager;


    @PostMapping("/login")
    public ResponseEntity<LoginResponce> loginAuth(@RequestBody LoginReq loginReq){

        log.info("Login details mail "+loginReq.getEmail() +"Password :"+loginReq.getPassword());

        Authentication authentication = authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(loginReq.getEmail() , loginReq.getPassword()));
        UserDetails userdetails = (UserDetails) authentication.getPrincipal();
        assert userdetails != null;
        log.info("User is valid now controller goes to generate token "+userdetails.getUsername()+" "+userdetails.getPassword());

        String token = jwtService.generateToken(userdetails);

        //generate token
        return ResponseEntity.ok(new LoginResponce(token));

    }

}
