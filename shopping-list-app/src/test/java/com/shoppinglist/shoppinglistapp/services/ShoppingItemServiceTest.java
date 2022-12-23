package com.shoppinglist.shoppinglistapp.services;

import com.shoppinglist.shoppinglistapp.dtos.ShoppingItemDto;
import com.shoppinglist.shoppinglistapp.enums.EStatus;
import com.shoppinglist.shoppinglistapp.enums.EUnit;
import com.shoppinglist.shoppinglistapp.exception.ApiExpectationFailedException;
import com.shoppinglist.shoppinglistapp.exception.ApiForbiddenException;
import com.shoppinglist.shoppinglistapp.exception.ApiNotFoundException;
import com.shoppinglist.shoppinglistapp.models.ShoppingItem;
import com.shoppinglist.shoppinglistapp.models.ShoppingList;
import com.shoppinglist.shoppinglistapp.models.User;
import com.shoppinglist.shoppinglistapp.repositories.ShoppingItemRepository;
import com.shoppinglist.shoppinglistapp.repositories.ShoppingListRepository;
import com.shoppinglist.shoppinglistapp.repositories.UserRepository;
import com.shoppinglist.shoppinglistapp.security.userdetails.UserDetailsI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShoppingItemServiceTest {

    @Mock
    private UserRepository testUserRepository;
    @Mock
    private ShoppingListRepository testShoppingListRepository;
    @Mock
    private ShoppingItemRepository testShoppingItemRepository;
    private ShoppingItemService testShoppingItemService;
    private User user;
    private ShoppingItem shoppingItem;
    private ShoppingList shoppingList;
    private final Long shoppingItemId = 1L;
    private final Long shoppingListId = 1L;
    private final EStatus statusWaiting = EStatus.WAITING;
    private final EStatus statusAccomplished = EStatus.ACCOMPLISHED;

    @BeforeEach
    void setUp() {
        testShoppingItemService =
                new ShoppingItemService(testShoppingItemRepository, testUserRepository, testShoppingListRepository);

        user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("test1@gmail.com");

        shoppingList = new ShoppingList();
        shoppingList.setId(shoppingListId);
        shoppingList.setUser(user);
        shoppingList.setTitle("lista");
        shoppingList.setStatus(statusWaiting);

        shoppingItem = new ShoppingItem();
        shoppingItem.setId(shoppingItemId);
        shoppingItem.setStatus(statusWaiting);
        shoppingItem.setName("produkt");
        shoppingItem.setQuantity(2d);
        shoppingItem.setUnit(EUnit.PIECES);
        shoppingItem.setShoppingList(shoppingList);
    }

    @Test
    void testGetShoppingItem() {
        Mockito.when(testShoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));
        ShoppingItem shoppingItemResponse = testShoppingItemService.getShoppingItem(shoppingItemId);
        verify(testShoppingItemRepository).findById(shoppingItemId);
        assertEquals(shoppingItemResponse, shoppingItem);
    }

    @Test
    void testGetShoppingItemNotFound() {
        Mockito.when(testShoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.empty());
        assertThrows(ApiNotFoundException.class, () -> testShoppingItemService.getShoppingItem(shoppingItemId));
    }

    @Test
    void testGetShoppingItems() {
        Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));
        Mockito.when(testShoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(
                shoppingList, Arrays.asList(statusWaiting,statusAccomplished))).thenReturn(Optional.of(List.of(shoppingItem)));

        List<ShoppingItem> shoppingItems = testShoppingItemService.getShoppingItems(shoppingListId);

        verify(testShoppingItemRepository).findShoppingItemByShoppingListAndStatusIn(
                shoppingList, Arrays.asList(statusWaiting,statusAccomplished));
        assertEquals(List.of(shoppingItem), shoppingItems);
    }

    @Test
    void testGetShoppingItemsListNotFound() {
        Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.empty());
        assertThrows(ApiNotFoundException.class, () -> testShoppingItemService.getShoppingItems(shoppingListId));
    }

    @Test
    void testAddShoppingItemListNotFound() {
        Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.empty());
        assertThrows(ApiNotFoundException.class,
                () -> testShoppingItemService.addShoppingItem(shoppingItem,shoppingListId));
    }

    @Nested
    class TestWithUser {

        @BeforeEach
        public void init() {
            UserDetailsI applicationUser = UserDetailsI.build(user);
            Authentication authentication = Mockito.mock(Authentication.class);
            SecurityContext securityContext = Mockito.mock(SecurityContext.class);
            Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);
            Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
        }

        @Test
        void testAddShoppingItem() {
            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));
            testShoppingItemService.addShoppingItem(shoppingItem, shoppingListId);

            ArgumentCaptor<ShoppingItem> shoppingItemArgumentCaptor = ArgumentCaptor.forClass(ShoppingItem.class);
            verify(testShoppingItemRepository).save(shoppingItemArgumentCaptor.capture());

            ShoppingItem captureShoppingItem = shoppingItemArgumentCaptor.getValue();

            assertThat(captureShoppingItem).isEqualTo(shoppingItem);
        }

        @Test
        void testAddShoppingItemImage() throws IOException {

            MockMultipartFile image = new MockMultipartFile("image", new byte[1]);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));

            testShoppingItemService.addShoppingItem(image, shoppingItem, shoppingListId);

            ArgumentCaptor<ShoppingItem> shoppingItemArgumentCaptor = ArgumentCaptor.forClass(ShoppingItem.class);
            verify(testShoppingItemRepository).save(shoppingItemArgumentCaptor.capture());

            ShoppingItem captureShoppingItem = shoppingItemArgumentCaptor.getValue();

            assertThat(captureShoppingItem).isEqualTo(shoppingItem);

        }

        @Test
        void testAddShoppingItemNotUserList() {
            User otherUser = new User();
            otherUser.setUsername("username2");
            otherUser.setPassword("password");
            otherUser.setEmail("test2@gmail.com");
            shoppingList.setUser(otherUser);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));

            assertThrows(ApiForbiddenException.class, () ->
                    testShoppingItemService.addShoppingItem(shoppingItem, shoppingListId));
        }

        @Test
        void testUpdateShoppingItemName() {
            String newName = "newName";

            ShoppingItemDto shoppingItemDto= new ShoppingItemDto();
            shoppingItemDto.setName(newName);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));

            testShoppingItemService.updateShoppingItem(shoppingItemId, shoppingItemDto);

            assertEquals(newName, shoppingItem.getName());
        }

        @Test
        void testUpdateShoppingItemQuantity() {
            Double newQuantity = 8d;

            ShoppingItemDto shoppingItemDto= new ShoppingItemDto();
            shoppingItemDto.setQuantity(newQuantity);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));

            testShoppingItemService.updateShoppingItem(shoppingItemId, shoppingItemDto);

            assertEquals(newQuantity, shoppingItem.getQuantity());
        }

        @Test
        void testUpdateShoppingItemUnit() {
            EUnit newUnit = EUnit.GRAMS;

            ShoppingItemDto shoppingItemDto= new ShoppingItemDto();
            shoppingItemDto.setUnit(newUnit);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));

            testShoppingItemService.updateShoppingItem(shoppingItemId, shoppingItemDto);

            assertEquals(newUnit, shoppingItem.getUnit());
        }

        @Test
        void testUpdateShoppingItemImage() throws IOException {

            ShoppingItemDto shoppingItemDto= new ShoppingItemDto();
            MockMultipartFile image = new MockMultipartFile("image", new byte[1]);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));

            testShoppingItemService.updateShoppingItem(shoppingItemId, image, shoppingItemDto);

            assertNotNull(shoppingItem.getImage());
        }

        @Test
        void testUpdateShoppingItemAccomplished() {
            ShoppingItemDto shoppingItemDto= new ShoppingItemDto();
            shoppingItemDto.setStatus(statusAccomplished);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));

            testShoppingItemService.updateShoppingItem(shoppingItemId, shoppingItemDto);

            assertEquals(statusAccomplished, shoppingItem.getStatus());
        }

        @Test
        void testUpdateShoppingItemAccomplishedListAccomplished() {
            ShoppingItemDto shoppingItemDto = new ShoppingItemDto();
            shoppingItemDto.setStatus(statusAccomplished);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));
            Mockito.when(testShoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(shoppingList,
                            List.of(EStatus.WAITING)))
                    .thenReturn(Optional.empty());

            testShoppingItemService.updateShoppingItem(shoppingItemId, shoppingItemDto);

            assertEquals(statusAccomplished, shoppingList.getStatus());
        }

        @Test
        void testUpdateShoppingItemWaitingListWaiting() {
            ShoppingItemDto shoppingItemDto = new ShoppingItemDto();
            shoppingItemDto.setStatus(statusWaiting);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));
            Mockito.when(testShoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(shoppingList,
                            List.of(EStatus.WAITING)))
                    .thenReturn(Optional.of(List.of(shoppingItem)));

            testShoppingItemService.updateShoppingItem(shoppingItemId, shoppingItemDto);

            assertEquals(statusWaiting, shoppingList.getStatus());
        }

        @Test
        void testUpdateShoppingItemNotUserList() {
            ShoppingItemDto shoppingItemDto = new ShoppingItemDto();

            User otherUser = new User();
            otherUser.setUsername("username2");
            otherUser.setPassword("password");
            otherUser.setEmail("test2@gmail.com");
            shoppingList.setUser(otherUser);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));

            assertThrows(ApiForbiddenException.class, () ->
                    testShoppingItemService.updateShoppingItem(shoppingItemId, shoppingItemDto));
        }

        @Test
        void testUpdateShoppingItemDeletedItem() {
            ShoppingItemDto shoppingItemDto = new ShoppingItemDto();

            shoppingItem.setStatus(EStatus.DELETED);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));

            assertThrows(ApiExpectationFailedException.class, () ->
                    testShoppingItemService.updateShoppingItem(shoppingListId, shoppingItemDto));
        }

        @Test
        void testDeleteShoppingItem() {
            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));

            testShoppingItemService.deleteShoppingItem(shoppingItemId);

            assertEquals(EStatus.DELETED, shoppingItem.getStatus());
        }

        @Test
        void testDeleteShoppingItemNotUserList() {
            User otherUser = new User();
            otherUser.setUsername("username2");
            otherUser.setPassword("password");
            otherUser.setEmail("test2@gmail.com");
            shoppingList.setUser(otherUser);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));

            assertThrows(ApiForbiddenException.class, () ->
                    testShoppingItemService.deleteShoppingItem(shoppingItemId));
        }

        @Test
        void testCannotFoundUser() {
            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.empty());
            Mockito.when(testShoppingItemRepository.findById(shoppingItemId)).thenReturn(Optional.of(shoppingItem));
            assertThrows(UsernameNotFoundException.class, () -> testShoppingItemService.deleteShoppingItem(shoppingItemId));
        }

    }
}