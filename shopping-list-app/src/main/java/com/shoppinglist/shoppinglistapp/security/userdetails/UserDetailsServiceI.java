package com.shoppinglist.shoppinglistapp.security.userdetails;

import com.shoppinglist.shoppinglistapp.models.User;
import com.shoppinglist.shoppinglistapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceI implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(s)
                .orElseThrow(() -> new UsernameNotFoundException("Cannot found user"));
        return UserDetailsI.build(user);
    }
}
