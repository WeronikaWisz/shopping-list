package com.shoppinglist.shoppinglistapp.controllers.manageusers;

import com.shoppinglist.shoppinglistapp.dtos.auth.JwtResponse;
import com.shoppinglist.shoppinglistapp.dtos.auth.LoginRequest;
import com.shoppinglist.shoppinglistapp.dtos.auth.MessageResponse;
import com.shoppinglist.shoppinglistapp.dtos.auth.SignupRequest;
import com.shoppinglist.shoppinglistapp.enums.ERole;
import com.shoppinglist.shoppinglistapp.models.Role;
import com.shoppinglist.shoppinglistapp.models.User;
import com.shoppinglist.shoppinglistapp.repositories.RoleRepository;
import com.shoppinglist.shoppinglistapp.repositories.UserRepository;
import com.shoppinglist.shoppinglistapp.security.jwt.JwtUtils;
import com.shoppinglist.shoppinglistapp.security.userdetails.UserDetailsI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@CrossOrigin("http://localhost:4200")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private MessageSource messageSource;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsI userDetails = (UserDetailsI) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(messageSource.getMessage(
                            "exception.usernameUsed", null, LocaleContextHolder.getLocale())));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(messageSource.getMessage(
                            "exception.emailUsed", null, LocaleContextHolder.getLocale())));
        }

        User user = new User(signUpRequest.getName(), signUpRequest.getSurname(), signUpRequest.getUsername(),
                encoder.encode(signUpRequest.getPassword()), signUpRequest.getEmail());

        user.setCreationDate(LocalDateTime.now());

        Set<Role> roles = new HashSet<>();

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException(messageSource.getMessage(
                        "exception.userRoleMissing", null, LocaleContextHolder.getLocale())));
        roles.add(userRole);

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse(messageSource.getMessage(
                "success.userRegister", null, LocaleContextHolder.getLocale())));
    }
}
