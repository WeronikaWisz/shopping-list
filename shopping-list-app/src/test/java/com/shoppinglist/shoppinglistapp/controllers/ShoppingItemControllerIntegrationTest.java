package com.shoppinglist.shoppinglistapp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppinglist.shoppinglistapp.dtos.ShoppingItemCardDto;
import com.shoppinglist.shoppinglistapp.dtos.ShoppingItemDto;
import com.shoppinglist.shoppinglistapp.enums.ERole;
import com.shoppinglist.shoppinglistapp.enums.EStatus;
import com.shoppinglist.shoppinglistapp.helpers.ImageHelper;
import com.shoppinglist.shoppinglistapp.models.Role;
import com.shoppinglist.shoppinglistapp.models.ShoppingItem;
import com.shoppinglist.shoppinglistapp.models.ShoppingList;
import com.shoppinglist.shoppinglistapp.models.User;
import com.shoppinglist.shoppinglistapp.repositories.RoleRepository;
import com.shoppinglist.shoppinglistapp.repositories.ShoppingItemRepository;
import com.shoppinglist.shoppinglistapp.repositories.ShoppingListRepository;
import com.shoppinglist.shoppinglistapp.repositories.UserRepository;
import com.shoppinglist.shoppinglistapp.security.WebSecurityConfig;
import com.shoppinglist.shoppinglistapp.services.ShoppingItemService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Import(WebSecurityConfig.class)
@WithUserDetails("username")
public class ShoppingItemControllerIntegrationTest {

    @MockBean
    private ShoppingListRepository shoppingListRepository;

    @MockBean
    private ShoppingItemRepository shoppingItemRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ShoppingItemService shoppingItemService;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
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
    }

    @PostConstruct
    public void saveUser(){
        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));
    }

    @Test
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

        when(shoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));
        when(shoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(
                shoppingList, Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED)))
                .thenReturn(Optional.of(shoppingItemList));

        MvcResult result = mockMvc.perform(get("/shopping-item/shopping-item/{shoppingListId}", shoppingListId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(objectMapper.writeValueAsString(shoppingItemsCardDto), result.getResponse().getContentAsString());
    }

    @Test
    void testAddShoppingItemNoImage() throws Exception {

        Long shoppingListId = 1L;
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setId(shoppingListId);
        shoppingList.setUser(user);

        ShoppingItemDto shoppingItemDto = new ShoppingItemDto();
        shoppingItemDto.setName("name");
        shoppingItemDto.setStatus(EStatus.WAITING);
        shoppingItemDto.setShoppingListId(shoppingListId);

        MockMultipartFile info = new MockMultipartFile("info", "",
                "application/json", objectMapper.writeValueAsBytes(shoppingItemDto));

        when(shoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));
        when(shoppingItemRepository.save(Mockito.any(ShoppingItem.class))).thenAnswer(i -> i.getArguments()[0]);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/shopping-item/shopping-item")
                        .file(info)
                        .with(csrf()))
                .andExpect(status().isOk());

        assertNotNull(shoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(shoppingList, List.of(EStatus.WAITING)));

    }

    @Test
    void testAddShoppingItemWithImage() throws Exception {

        Long shoppingListId = 1L;
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setId(shoppingListId);
        shoppingList.setUser(user);

        ShoppingItemDto shoppingItemDto = new ShoppingItemDto();
        shoppingItemDto.setName("name");
        shoppingItemDto.setStatus(EStatus.WAITING);
        shoppingItemDto.setShoppingListId(shoppingListId);

        MockMultipartFile info = new MockMultipartFile("info", "",
                "application/json", objectMapper.writeValueAsBytes(shoppingItemDto));
        MockMultipartFile image = new MockMultipartFile("image", new byte[1]);

        when(shoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));
        when(shoppingItemRepository.save(Mockito.any(ShoppingItem.class))).thenAnswer(i -> i.getArguments()[0]);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/shopping-item/shopping-item")
                        .file(image)
                        .file(info)
                        .with(csrf()))
                .andExpect(status().isOk());

        assertNotNull(shoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(shoppingList, List.of(EStatus.WAITING)));

    }

    @Test
    void testUpdateShoppingItemNoImage() throws Exception {

        Long shoppingListId = 1L;
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setId(shoppingListId);
        shoppingList.setUser(user);

        Long shoppingItemId = 1L;
        ShoppingItem shoppingItem = new ShoppingItem();
        shoppingItem.setName("oldName");
        shoppingItem.setStatus(EStatus.WAITING);
        shoppingItem.setShoppingList(shoppingList);

        ShoppingItemDto shoppingItemDto = new ShoppingItemDto();
        shoppingItemDto.setName("newName");
        shoppingItemDto.setStatus(EStatus.ACCOMPLISHED);

        MockMultipartFile info = new MockMultipartFile("info", "",
                "application/json", objectMapper.writeValueAsBytes(shoppingItemDto));

        when(shoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));

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

        assertEquals(EStatus.ACCOMPLISHED, shoppingItem.getStatus());
        assertEquals("newName", shoppingItem.getName());

    }

    @Test
    void testUpdateShoppingItemWithImage() throws Exception {

        Long shoppingListId = 1L;
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setId(shoppingListId);
        shoppingList.setUser(user);

        Long shoppingItemId = 1L;
        ShoppingItem shoppingItem = new ShoppingItem();
        shoppingItem.setName("oldName");
        shoppingItem.setStatus(EStatus.WAITING);
        shoppingItem.setShoppingList(shoppingList);

        ShoppingItemDto shoppingItemDto = new ShoppingItemDto();

        MockMultipartFile info = new MockMultipartFile("info", "",
                "application/json", objectMapper.writeValueAsBytes(shoppingItemDto));
        MockMultipartFile image = new MockMultipartFile("image", new byte[1]);

        when(shoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));

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

        assertNotNull(shoppingItem.getImage());

    }
    @Test
    void testGetShoppingItemImage() throws Exception {

        Long shoppingItemId = 1L;

        ShoppingItem shoppingItem = new ShoppingItem();
        shoppingItem.setImage(ImageHelper.compressBytes(new byte[1]));

        when(shoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));

        mockMvc.perform(get("/shopping-item/shopping-item/{id}/image", shoppingItemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testDeleteShoppingItem() throws Exception {

        Long shoppingListId = 1L;
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setId(shoppingListId);
        shoppingList.setUser(user);

        Long id = 1L;
        ShoppingItem shoppingItem = new ShoppingItem();
        shoppingItem.setShoppingList(shoppingList);

        when(shoppingItemRepository.findById(id)).thenReturn(Optional.of(shoppingItem));

        mockMvc.perform(delete("/shopping-item/shopping-item/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(EStatus.DELETED, shoppingItem.getStatus());
        assertNotNull(shoppingItem.getDeleteDate());
    }


}
