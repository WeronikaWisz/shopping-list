package com.shoppinglist.shoppinglistapp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppinglist.shoppinglistapp.dtos.ShoppingListDto;
import com.shoppinglist.shoppinglistapp.enums.EStatus;
import com.shoppinglist.shoppinglistapp.models.ShoppingList;
import com.shoppinglist.shoppinglistapp.security.WebSecurityConfig;
import com.shoppinglist.shoppinglistapp.security.jwt.AuthEntryPointJwt;
import com.shoppinglist.shoppinglistapp.security.jwt.JwtUtils;
import com.shoppinglist.shoppinglistapp.security.userdetails.UserDetailsServiceI;
import com.shoppinglist.shoppinglistapp.services.ShoppingListService;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShoppingListController.class)
@Import(WebSecurityConfig.class)
class ShoppingListControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private ShoppingListService testShoppingListService;

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
    void testGetShoppingLists() throws Exception {

        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setId(1L);

        List<ShoppingList> shoppingListList = new ArrayList<>();
        shoppingListList.add(shoppingList);

        ShoppingListDto shoppingListDto = new ShoppingListDto();
        shoppingListDto.setId(1L);
        List<ShoppingListDto> shoppingListsDto = new ArrayList<>();
        shoppingListsDto.add(shoppingListDto);

        when(testShoppingListService.getShoppingLists()).thenReturn(shoppingListList);
        when(modelMapper.map(shoppingList, ShoppingListDto.class)).thenReturn(shoppingListDto);

        MvcResult result = mockMvc.perform(get("/shopping-list/shopping-list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(objectMapper.writeValueAsString(shoppingListsDto), result.getResponse().getContentAsString());

        verify(testShoppingListService).getShoppingLists();
    }

    @Test
    @WithMockUser
    void testAddShoppingList() throws Exception {

        ShoppingListDto shoppingListDto = new ShoppingListDto();
        shoppingListDto.setTitle("title");
        shoppingListDto.setStatus(EStatus.WAITING);

        ShoppingList shoppingList = modelMapper.map(shoppingListDto, ShoppingList.class);

        Mockito.doNothing().when(testShoppingListService).addShoppingList(shoppingList);

        mockMvc.perform(post("/shopping-list/shopping-list")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(shoppingListDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(testShoppingListService).addShoppingList(shoppingList);
    }

    @Test
    @WithMockUser
    void testUpdateShoppingList() throws Exception {

        Long shoppingListId = 1L;

        ShoppingListDto shoppingListDto = new ShoppingListDto();
        shoppingListDto.setTitle("title");
        shoppingListDto.setStatus(EStatus.WAITING);

        Mockito.doNothing().when(testShoppingListService).updateShoppingList(shoppingListId, shoppingListDto);

        mockMvc.perform(put("/shopping-list/shopping-list/{shoppingListId}", shoppingListId)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(shoppingListDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(testShoppingListService).updateShoppingList(shoppingListId, shoppingListDto);
    }

    @Test
    @WithMockUser
    void testDeleteShoppingList() throws Exception {
        Long id = 1L;

        Mockito.doNothing().when(testShoppingListService).deleteShoppingList(id);

        mockMvc.perform(delete("/shopping-list/shopping-list/{shoppingListId}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(testShoppingListService).deleteShoppingList(id);
    }
}