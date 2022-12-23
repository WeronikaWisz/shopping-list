package com.shoppinglist.shoppinglistapp.services;

import com.shoppinglist.shoppinglistapp.dtos.ShoppingListDto;
import com.shoppinglist.shoppinglistapp.enums.EStatus;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock
    private UserRepository testUserRepository;
    @Mock
    private ShoppingListRepository testShoppingListRepository;
    @Mock
    private ShoppingItemRepository testShoppingItemRepository;
    private ShoppingListService testShoppingListService;
    private User user;
    private ShoppingList shoppingList;
    private final Long shoppingListId = 1L;
    private final EStatus statusWaiting = EStatus.WAITING;
    private final EStatus statusAccomplished = EStatus.ACCOMPLISHED;
    private final DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @BeforeEach
    void setUp() {
        testShoppingListService =
                new ShoppingListService(testShoppingListRepository, testUserRepository, testShoppingItemRepository);

        user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("test1@gmail.com");

        shoppingList = new ShoppingList();
        shoppingList.setId(shoppingListId);
        shoppingList.setUser(user);
        shoppingList.setTitle("lista");
        shoppingList.setExecutionDate(LocalDateTime.parse("2023-04-08 12:30", dataFormatter));
        shoppingList.setStatus(statusWaiting);
    }

    @Test
    void testGetShoppingList() {
        Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));
        ShoppingList shoppingListResponse = testShoppingListService.getShoppingList(shoppingListId);
        verify(testShoppingListRepository).findById(shoppingListId);
        assertEquals(shoppingListResponse, shoppingList);
    }

    @Test
    void testGetShoppingListNotFound() {
        Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.empty());
        assertThrows(ApiNotFoundException.class, () -> testShoppingListService.getShoppingList(shoppingListId));
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
        void testGetShoppingLists() {
            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingListRepository
                            .findShoppingListByStatusInAndUser(Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED), user))
                    .thenReturn(Optional.of(List.of(shoppingList)));
            List<ShoppingList> shoppingLists = testShoppingListService.getShoppingLists();
            verify(testShoppingListRepository)
                    .findShoppingListByStatusInAndUser(Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED), user);
            assertEquals(List.of(shoppingList), shoppingLists);
        }

        @Test
        void testGetShoppingListsEmpty() {
            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingListRepository
                            .findShoppingListByStatusInAndUser(Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED), user))
                    .thenReturn(Optional.of(Collections.emptyList()));
            List<ShoppingList> shoppingLists = testShoppingListService.getShoppingLists();
            verify(testShoppingListRepository)
                    .findShoppingListByStatusInAndUser(Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED), user);
            assertEquals(Collections.emptyList(), shoppingLists);
        }

        @Test
        void testCannotFoundUser() {
            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.empty());
            assertThrows(UsernameNotFoundException.class, () -> testShoppingListService.getShoppingLists());
        }

        @Test
        void testAddShoppingList() {
            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            testShoppingListService.addShoppingList(shoppingList);

            ArgumentCaptor<ShoppingList> shoppingListArgumentCaptor = ArgumentCaptor.forClass(ShoppingList.class);
            verify(testShoppingListRepository).save(shoppingListArgumentCaptor.capture());

            ShoppingList captureShoppingList = shoppingListArgumentCaptor.getValue();

            assertThat(captureShoppingList).isEqualTo(shoppingList);

        }

        @Test
        void testUpdateShoppingListTitle() {
            String newTitle = "newTitle";

            ShoppingListDto shoppingListDto = new ShoppingListDto();
            shoppingListDto.setTitle(newTitle);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));

            testShoppingListService.updateShoppingList(shoppingListId, shoppingListDto);

            assertEquals(newTitle, shoppingList.getTitle());
        }

        @Test
        void testUpdateShoppingListExecutionDate() {
            String newExecutionDate = "2023-05-15 12:30";

            ShoppingListDto shoppingListDto = new ShoppingListDto();
            shoppingListDto.setExecutionDate(LocalDateTime.parse(newExecutionDate, dataFormatter));

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));

            testShoppingListService.updateShoppingList(shoppingListId, shoppingListDto);

            assertEquals(LocalDateTime.parse(newExecutionDate, dataFormatter), shoppingList.getExecutionDate());
        }

        @Test
        void testUpdateShoppingListAccomplished() {
            EStatus newStatus = EStatus.ACCOMPLISHED;

            ShoppingListDto shoppingListDto = new ShoppingListDto();
            shoppingListDto.setStatus(newStatus);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));

            testShoppingListService.updateShoppingList(shoppingListId, shoppingListDto);

            assertEquals(newStatus, shoppingList.getStatus());
        }

        @Test
        void testUpdateShoppingListAccomplishedAllItemAccomplished() {
            ShoppingItem shoppingItem1 = new ShoppingItem();
            shoppingItem1.setShoppingList(shoppingList);
            shoppingItem1.setStatus(statusAccomplished);
            ShoppingItem shoppingItem2 = new ShoppingItem();
            shoppingItem2.setShoppingList(shoppingList);
            shoppingItem1.setStatus(statusWaiting);

            ShoppingListDto shoppingListDto = new ShoppingListDto();
            shoppingListDto.setStatus(statusAccomplished);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));
            Mockito.when(testShoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(shoppingList,
                    Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED)))
                    .thenReturn(Optional.of(Arrays.asList(shoppingItem1, shoppingItem2)));

            testShoppingListService.updateShoppingList(shoppingListId, shoppingListDto);

            assertAll("All items should be accomplished when list is accomplished",
                    ()-> assertEquals(statusAccomplished, shoppingItem1.getStatus()),
                    ()-> assertEquals(statusAccomplished, shoppingItem2.getStatus()));
        }

        @Test
        void testUpdateShoppingListWaitingAllItemWaiting() {
            ShoppingItem shoppingItem1 = new ShoppingItem();
            shoppingItem1.setShoppingList(shoppingList);
            shoppingItem1.setStatus(statusAccomplished);
            ShoppingItem shoppingItem2 = new ShoppingItem();
            shoppingItem2.setShoppingList(shoppingList);
            shoppingItem1.setStatus(statusWaiting);

            ShoppingListDto shoppingListDto = new ShoppingListDto();
            shoppingListDto.setStatus(statusWaiting);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));
            Mockito.when(testShoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(shoppingList,
                            Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED)))
                    .thenReturn(Optional.of(Arrays.asList(shoppingItem1, shoppingItem2)));

            testShoppingListService.updateShoppingList(shoppingListId, shoppingListDto);

            assertAll("All items should be accomplished when list is accomplished",
                    ()-> assertEquals(statusWaiting, shoppingItem1.getStatus()),
                    ()-> assertEquals(statusWaiting, shoppingItem2.getStatus()));
        }

        @Test
        void testUpdateShoppingListNotUserList() {
            ShoppingListDto shoppingListDto = new ShoppingListDto();

            User otherUser = new User();
            otherUser.setUsername("username2");
            otherUser.setPassword("password");
            otherUser.setEmail("test2@gmail.com");
            shoppingList.setUser(otherUser);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));

            assertThrows(ApiForbiddenException.class, () ->
                    testShoppingListService.updateShoppingList(shoppingListId, shoppingListDto));
        }

        @Test
        void testUpdateShoppingListDeletedList() {
            ShoppingListDto shoppingListDto = new ShoppingListDto();

            shoppingList.setStatus(EStatus.DELETED);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));

            assertThrows(ApiExpectationFailedException.class, () ->
                    testShoppingListService.updateShoppingList(shoppingListId, shoppingListDto));
        }

        @Test
        void testDeleteShoppingList() {
            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));

            testShoppingListService.deleteShoppingList(shoppingListId);

            assertEquals(EStatus.DELETED, shoppingList.getStatus());
        }

        @Test
        void testDeleteShoppingListNotUserList() {
            User otherUser = new User();
            otherUser.setUsername("username2");
            otherUser.setPassword("password");
            otherUser.setEmail("test2@gmail.com");
            shoppingList.setUser(otherUser);

            Mockito.when(testUserRepository.findByUsername("username")).thenReturn(java.util.Optional.of(user));
            Mockito.when(testShoppingListRepository.findById(shoppingListId)).thenReturn(Optional.of(shoppingList));

            assertThrows(ApiForbiddenException.class, () ->
                    testShoppingListService.deleteShoppingList(shoppingListId));
        }

    }

}