package com.shoppinglist.shoppinglistapp.services;

import com.shoppinglist.shoppinglistapp.dtos.auth.ChangePassword;
import com.shoppinglist.shoppinglistapp.exception.ApiBadRequestException;
import com.shoppinglist.shoppinglistapp.models.User;
import com.shoppinglist.shoppinglistapp.repositories.UserRepository;
import com.shoppinglist.shoppinglistapp.security.userdetails.UserDetailsI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsersService {

    private final UserRepository userRepository;
    PasswordEncoder encoder;

    @Autowired
    public UsersService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Transactional
    public void changePassword(ChangePassword changePassword){
        String username = getCurrentUserUsername();
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Cannot found user"));
        if(encoder.matches(changePassword.getOldPassword(), user.getPassword())){
            if(encoder.matches(changePassword.getNewPassword(), user.getPassword())){
                throw new ApiBadRequestException("exception.newPasswordSameAsOld");
            } else {
                user.setPassword(encoder.encode(changePassword.getNewPassword()));
            }
        } else {
            throw new ApiBadRequestException("exception.wrongOldPassword");
        }
    }

    private String getCurrentUserUsername(){
        UserDetailsI userDetails = (UserDetailsI) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return userDetails.getUsername();
    }
}
