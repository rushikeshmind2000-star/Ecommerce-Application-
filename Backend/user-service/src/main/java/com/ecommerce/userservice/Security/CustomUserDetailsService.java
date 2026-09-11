package com.ecommerce.userservice.Security;

import com.ecommerce.userservice.Entity.UserEntity;
import com.ecommerce.userservice.Repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepo userRepo;

    public CustomUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity user = userRepo.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("user not found "));

        log.info("User details by CustomUserDetailsService  "+user.getEmail() +"Password :"+user.getPassword() +"Role "+user.getRole());

        return new CustomUserDetails(user);
    }
}
