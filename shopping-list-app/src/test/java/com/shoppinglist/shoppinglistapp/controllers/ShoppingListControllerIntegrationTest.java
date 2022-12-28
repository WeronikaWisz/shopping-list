package com.shoppinglist.shoppinglistapp.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppinglist.shoppinglistapp.dtos.ShoppingListDto;
import com.shoppinglist.shoppinglistapp.enums.ERole;
import com.shoppinglist.shoppinglistapp.enums.EStatus;
import com.shoppinglist.shoppinglistapp.models.Role;
import com.shoppinglist.shoppinglistapp.models.ShoppingList;
import com.shoppinglist.shoppinglistapp.models.User;
import com.shoppinglist.shoppinglistapp.repositories.RoleRepository;
import com.shoppinglist.shoppinglistapp.repositories.ShoppingItemRepository;
import com.shoppinglist.shoppinglistapp.repositories.ShoppingListRepository;
import com.shoppinglist.shoppinglistapp.repositories.UserRepository;
import com.shoppinglist.shoppinglistapp.security.WebSecurityConfig;
import com.shoppinglist.shoppinglistapp.services.ShoppingListService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Import(WebSecurityConfig.class)
public class ShoppingListControllerIntegrationTest {

    @MockBean
    private ShoppingListRepository shoppingListRepository;

    @MockBean
    private ShoppingItemRepository shoppingItemRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ShoppingListService shoppingListService;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private ModelMapper modelMapper;

    String username = "username";
    String password = "password";
    User user = new User();

    @BeforeAll
    public void init(){
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.context)
                .apply(springSecurity())
                .build();

        user.setUsername(username);
        user.setPassword(password);
        Optional<Role> userRole = roleRepository.findByName(ERole.ROLE_USER);
        userRole.ifPresent(role -> user.getRoles().add(role));
        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));
    }

//    @BeforeEach
//    void setUp() {
//        this.mockMvc = MockMvcBuilders
//                .webAppContextSetup(this.context)
//                .apply(springSecurity())
//                .build();
////        messageSource = Mockito.mock(MessageSource.class);
////        when(messageSource.getMessage(anyString(), any(Object[].class),any(Locale.class))).thenReturn("");
////        delegatingMessageSource.setParentMessageSource(messageSource);
//
//        user.setUsername(username);
//        user.setPassword(password);
//        when(userRepository.findByUsername("username")).thenReturn(java.util.Optional.ofNullable(user));
//    }

    @Test
    @WithUserDetails("username")
    void testGetShoppingLists() throws Exception {

        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setId(1L);
        shoppingList.setUser(user);

        List<ShoppingList> shoppingListList = new ArrayList<>();
        shoppingListList.add(shoppingList);

        ShoppingListDto shoppingListDto = new ShoppingListDto();
        shoppingListDto.setId(1L);
        List<ShoppingListDto> shoppingListsDto = new ArrayList<>();
        shoppingListsDto.add(shoppingListDto);

        given(shoppingListRepository.findShoppingListByStatusInAndUser(
                Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED), user))
                .willReturn(java.util.Optional.of(shoppingListList));
        given(modelMapper.map(shoppingList, ShoppingListDto.class)).willReturn(shoppingListDto);

        MvcResult result =
                mockMvc.perform(get("/shopping-list/shopping-list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(objectMapper.writeValueAsString(shoppingListsDto), result.getResponse().getContentAsString());
    }

}
