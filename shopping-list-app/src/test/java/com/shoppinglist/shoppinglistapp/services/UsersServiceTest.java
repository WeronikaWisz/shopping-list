package com.shoppinglist.shoppinglistapp.services;

import com.shoppinglist.shoppinglistapp.dtos.auth.ChangePassword;
import com.shoppinglist.shoppinglistapp.exception.ApiBadRequestException;
import com.shoppinglist.shoppinglistapp.models.User;
import com.shoppinglist.shoppinglistapp.repositories.UserRepository;
import com.shoppinglist.shoppinglistapp.security.userdetails.UserDetailsI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @Mock
    private UserRepository testUserRepository;
    private UsersService testUserService;
    private User user;
    @Mock
    PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        testUserService = new UsersService(testUserRepository, encoder);
        user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("test1@gmail.com");
        UserDetailsI applicationUser = UserDetailsI.build(user);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
    }

    @Test
    void testChangePassword() {
        String oldPassword = "password";
        String newPassword = "passwordNew";
        ChangePassword changePassword = new ChangePassword();
        changePassword.setOldPassword(oldPassword);
        changePassword.setNewPassword(newPassword);

        Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
        Mockito.when(encoder.matches(oldPassword, user.getPassword())).thenReturn(true);
        Mockito.when(encoder.matches(newPassword, user.getPassword())).thenReturn(false);
        Mockito.when(encoder.encode(changePassword.getNewPassword())).thenReturn(newPassword);

        testUserService.changePassword(changePassword);

        assertEquals(newPassword, user.getPassword());
    }

    @Test
    void testChangePasswordSameNewOld() {
        String oldPassword = "password";
        String newPassword = "password";
        ChangePassword changePassword = new ChangePassword();
        changePassword.setOldPassword(oldPassword);
        changePassword.setNewPassword(newPassword);

        Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
        Mockito.when(encoder.matches(oldPassword, user.getPassword())).thenReturn(true);

        assertThrows(ApiBadRequestException.class, () -> testUserService.changePassword(changePassword));
    }

    @Test
    void testChangePasswordUserNotFound() {
        Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.empty());
        ChangePassword changePassword = new ChangePassword();
        assertThrows(UsernameNotFoundException.class, () -> testUserService.changePassword(changePassword));
    }

    @Test
    void testChangePasswordWrongOld() {
        Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
        String newPassword = "passwordNew";
        ChangePassword changePassword = new ChangePassword();
        changePassword.setOldPassword("passwordWrong");
        changePassword.setNewPassword(newPassword);
        assertThrows(ApiBadRequestException.class, () -> testUserService.changePassword(changePassword));
    }
}