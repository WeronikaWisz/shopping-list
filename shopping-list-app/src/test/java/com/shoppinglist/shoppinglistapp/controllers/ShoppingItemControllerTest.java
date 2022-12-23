package com.shoppinglist.shoppinglistapp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppinglist.shoppinglistapp.dtos.ShoppingItemCardDto;
import com.shoppinglist.shoppinglistapp.dtos.ShoppingItemDto;
import com.shoppinglist.shoppinglistapp.enums.EStatus;
import com.shoppinglist.shoppinglistapp.helpers.ImageHelper;
import com.shoppinglist.shoppinglistapp.models.ShoppingItem;
import com.shoppinglist.shoppinglistapp.models.ShoppingList;
import com.shoppinglist.shoppinglistapp.security.WebSecurityConfig;
import com.shoppinglist.shoppinglistapp.security.jwt.AuthEntryPointJwt;
import com.shoppinglist.shoppinglistapp.security.jwt.JwtUtils;
import com.shoppinglist.shoppinglistapp.security.userdetails.UserDetailsServiceI;
import com.shoppinglist.shoppinglistapp.services.ShoppingItemService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShoppingItemController.class)
@Import(WebSecurityConfig.class)
class ShoppingItemControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private ShoppingItemService testShoppingItemService;

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
    void testGetShoppingItems() throws Exception {

        Long shoppingListId = 1L;
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setId(shoppingListId);

        ShoppingItem shoppingItem = new ShoppingItem();
        shoppingItem.setId(1L);
        shoppingItem.setShoppingList(shoppingList);

        List<ShoppingItem> shoppingItemList = new ArrayList<>();
        shoppingItemList.add(shoppingItem);

        ShoppingItemCardDto shoppingItemCardDto = new ShoppingItemCardDto();
        shoppingItemCardDto.setShoppingListId(shoppingListId);
        shoppingItemCardDto.setId(1L);

        List<ShoppingItemCardDto> shoppingItemsCardDto = new ArrayList<>();
        shoppingItemsCardDto.add(shoppingItemCardDto);

        when(testShoppingItemService.getShoppingItems(shoppingListId)).thenReturn(shoppingItemList);
        when(modelMapper.map(shoppingItem, ShoppingItemCardDto.class)).thenReturn(shoppingItemCardDto);

        MvcResult result = mockMvc.perform(get("/shopping-item/shopping-item/{shoppingListId}", shoppingListId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(objectMapper.writeValueAsString(shoppingItemsCardDto), result.getResponse().getContentAsString());

        verify(testShoppingItemService).getShoppingItems(shoppingListId);
    }

    @Test
    @WithMockUser
    void testAddShoppingItemNoImage() throws Exception {

        Long shoppingListId = 1L;

        ShoppingItemDto shoppingItemDto = new ShoppingItemDto();
        shoppingItemDto.setName("name");
        shoppingItemDto.setStatus(EStatus.WAITING);
        shoppingItemDto.setShoppingListId(shoppingListId);

        ShoppingItem shoppingItem = modelMapper.map(shoppingItemDto, ShoppingItem.class);

        MockMultipartFile info = new MockMultipartFile("info", "",
                "application/json", objectMapper.writeValueAsBytes(shoppingItemDto));

        Mockito.doNothing().when(testShoppingItemService).addShoppingItem(shoppingItem, shoppingListId);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/shopping-item/shopping-item")
                        .file(info)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(testShoppingItemService).addShoppingItem(shoppingItem, shoppingListId);

    }

    @Test
    @WithMockUser
    void testAddShoppingItemWithImage() throws Exception {

        Long shoppingListId = 1L;

        ShoppingItemDto shoppingItemDto = new ShoppingItemDto();
        shoppingItemDto.setName("name");
        shoppingItemDto.setStatus(EStatus.WAITING);
        shoppingItemDto.setShoppingListId(shoppingListId);

        ShoppingItem shoppingItem = modelMapper.map(shoppingItemDto, ShoppingItem.class);

        MockMultipartFile info = new MockMultipartFile("info", "",
                "application/json", objectMapper.writeValueAsBytes(shoppingItemDto));
        MockMultipartFile image = new MockMultipartFile("image", new byte[1]);

        Mockito.doNothing().when(testShoppingItemService).addShoppingItem(image, shoppingItem, shoppingListId);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/shopping-item/shopping-item")
                        .file(image)
                        .file(info)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(testShoppingItemService).addShoppingItem(image, shoppingItem, shoppingListId);

    }

    @Test
    @WithMockUser
    void testAddShoppingItemWithImageThrowIOException() throws Exception {

        Long shoppingListId = 1L;

        ShoppingItemDto shoppingItemDto = new ShoppingItemDto();
        shoppingItemDto.setName("name");
        shoppingItemDto.setStatus(EStatus.WAITING);
        shoppingItemDto.setShoppingListId(shoppingListId);

        ShoppingItem shoppingItem = modelMapper.map(shoppingItemDto, ShoppingItem.class);

        MockMultipartFile info = new MockMultipartFile("info", "",
                "application/json", objectMapper.writeValueAsBytes(shoppingItemDto));
        MockMultipartFile image = new MockMultipartFile("image", new byte[1]);

        Mockito.doThrow(new IOException()).when(testShoppingItemService).addShoppingItem(image, shoppingItem, shoppingListId);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/shopping-item/shopping-item")
                        .file(image)
                        .file(info)
                        .with(csrf()))
                .andExpect(status().isExpectationFailed());

    }

    @Test
    @WithMockUser
    void testUpdateShoppingItemNoImage() throws Exception {

        Long shoppingItemId = 1L;

        ShoppingItemDto shoppingItemDto = new ShoppingItemDto();
        shoppingItemDto.setName("name");
        shoppingItemDto.setStatus(EStatus.WAITING);

        MockMultipartFile info = new MockMultipartFile("info", "",
                "application/json", objectMapper.writeValueAsBytes(shoppingItemDto));

        Mockito.doNothing().when(testShoppingItemService).updateShoppingItem(shoppingItemId, shoppingItemDto);

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart("/shopping-item/shopping-item/{id}", shoppingItemId);
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mockMvc.perform(builder
                        .file(info)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(testShoppingItemService).updateShoppingItem(shoppingItemId, shoppingItemDto);

    }

    @Test
    @WithMockUser
    void testUpdateShoppingItemWithImage() throws Exception {

        Long shoppingItemId = 1L;

        ShoppingItemDto shoppingItemDto = new ShoppingItemDto();
        shoppingItemDto.setName("name");
        shoppingItemDto.setStatus(EStatus.WAITING);

        MockMultipartFile info = new MockMultipartFile("info", "",
                "application/json", objectMapper.writeValueAsBytes(shoppingItemDto));
        MockMultipartFile image = new MockMultipartFile("image", new byte[1]);

        Mockito.doNothing().when(testShoppingItemService).updateShoppingItem(shoppingItemId, image, shoppingItemDto);

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart("/shopping-item/shopping-item/{id}", shoppingItemId);
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mockMvc.perform(builder
                        .file(info)
                        .file(image)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(testShoppingItemService).updateShoppingItem(shoppingItemId, image, shoppingItemDto);

    }

    @Test
    @WithMockUser
    void testUpdateShoppingItemWithImageThrowIOException() throws Exception {

        Long shoppingItemId = 1L;

        ShoppingItemDto shoppingItemDto = new ShoppingItemDto();
        shoppingItemDto.setName("name");
        shoppingItemDto.setStatus(EStatus.WAITING);

        MockMultipartFile info = new MockMultipartFile("info", "",
                "application/json", objectMapper.writeValueAsBytes(shoppingItemDto));
        MockMultipartFile image = new MockMultipartFile("image", new byte[1]);

        Mockito.doThrow(new IOException()).when(testShoppingItemService).updateShoppingItem(shoppingItemId, image, shoppingItemDto);

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart("/shopping-item/shopping-item/{id}", shoppingItemId);
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mockMvc.perform(builder
                        .file(info)
                        .file(image)
                        .with(csrf()))
                .andExpect(status().isExpectationFailed());

    }

    @Test
    @WithMockUser
    void testGetShoppingItemImageNoImage() throws Exception {

        Long shoppingItemId = 1L;

        ShoppingItem shoppingItem = new ShoppingItem();

        when(testShoppingItemService.getShoppingItem(shoppingItemId)).thenReturn(shoppingItem);

        mockMvc.perform(get("/shopping-item/shopping-item/{id}/image", shoppingItemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isExpectationFailed());

        verify(testShoppingItemService).getShoppingItem(shoppingItemId);
    }

    @Test
    @WithMockUser
    void testGetShoppingItemImageHasImage() throws Exception {

        Long shoppingItemId = 1L;

        ShoppingItem shoppingItem = new ShoppingItem();
        shoppingItem.setImage(ImageHelper.compressBytes(new byte[1]));

        when(testShoppingItemService.getShoppingItem(shoppingItemId)).thenReturn(shoppingItem);

        mockMvc.perform(get("/shopping-item/shopping-item/{id}/image", shoppingItemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(testShoppingItemService).getShoppingItem(shoppingItemId);
    }

    @Test
    @WithMockUser
    void testDeleteShoppingItem() throws Exception {
        Long id = 1L;

        Mockito.doNothing().when(testShoppingItemService).deleteShoppingItem(id);

        mockMvc.perform(delete("/shopping-item/shopping-item/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(testShoppingItemService).deleteShoppingItem(id);
    }
}