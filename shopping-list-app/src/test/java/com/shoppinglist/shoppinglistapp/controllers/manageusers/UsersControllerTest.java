package com.shoppinglist.shoppinglistapp.controllers.manageusers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppinglist.shoppinglistapp.dtos.auth.ChangePassword;
import com.shoppinglist.shoppinglistapp.security.WebSecurityConfig;
import com.shoppinglist.shoppinglistapp.security.jwt.AuthEntryPointJwt;
import com.shoppinglist.shoppinglistapp.security.jwt.JwtUtils;
import com.shoppinglist.shoppinglistapp.security.userdetails.UserDetailsServiceI;
import com.shoppinglist.shoppinglistapp.services.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.DelegatingMessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UsersController.class)
@Import(WebSecurityConfig.class)
class UsersControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private UsersService testUsersService;

    @MockBean
    private ModelMapper modelMapper;

    private MessageSource messageSource;

    @Autowired
    private DelegatingMessageSource delegatingMessageSource;

    @MockBean
    private UserDetailsServiceI userDetailsServiceI;

    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;

    @MockBean
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.context)
                .apply(springSecurity())
                .build();
        messageSource = Mockito.mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(Object[].class),any(Locale.class))).thenReturn("");
        delegatingMessageSource.setParentMessageSource(messageSource);
    }

    @Test
    @WithMockUser
    void testChangePassword() throws Exception {
        ChangePassword changePassword = new ChangePassword();
        changePassword.setOldPassword("password");
        changePassword.setNewPassword("newPassword");

        Mockito.doNothing().when(testUsersService).changePassword(changePassword);

        mockMvc.perform(put("/users/user/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePassword)))
                .andExpect(status().isOk());

        verify(testUsersService).changePassword(changePassword);
    }
}