package com.shoppinglist.shoppinglistapp.services;

import com.shoppinglist.shoppinglistapp.dtos.ShoppingListDto;
import com.shoppinglist.shoppinglistapp.enums.EStatus;
import com.shoppinglist.shoppinglistapp.models.ShoppingList;
import com.shoppinglist.shoppinglistapp.models.User;
import com.shoppinglist.shoppinglistapp.repositories.RoleRepository;
import com.shoppinglist.shoppinglistapp.repositories.ShoppingListRepository;
import com.shoppinglist.shoppinglistapp.repositories.UserRepository;
import com.shoppinglist.shoppinglistapp.security.WebSecurityConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Import(WebSecurityConfig.class)
@WithUserDetails("username")
public class ShoppingListServiceIntegrationTest {

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ShoppingListService shoppingListService;

    private Long userId;
    User user = new User();

    ShoppingList shoppingList = new ShoppingList();
    private Long shoppingListId;
    private final EStatus statusWaiting = EStatus.WAITING;
    private final EStatus statusAccomplished = EStatus.ACCOMPLISHED;
    private final EStatus statusDeleted = EStatus.DELETED;
    private final DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @PostConstruct
    public void saveUser(){
        String username = "username";
        user.setUsername(username);
        String password = "password";
        user.setPassword(password);
        String email = "test1@gmail.com";
        user.setEmail(email);
        User savedUser = userRepository.save(user);
        userId = savedUser.getId();

        shoppingList.setUser(savedUser);
        shoppingList.setTitle("lista");
        shoppingList.setExecutionDate(LocalDateTime.parse("2023-04-08 12:30", dataFormatter));
        shoppingList.setStatus(statusWaiting);
        ShoppingList savedShoppingList = shoppingListRepository.save(shoppingList);
        shoppingListId = savedShoppingList.getId();
    }

    @AfterAll
    public void afterAll() {
        Optional<List<ShoppingList>> shoppingLists = shoppingListRepository.findShoppingListByStatusInAndUser(
                Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED, EStatus.DELETED), user);
        shoppingLists.ifPresent(lists -> lists.forEach(sl -> shoppingListRepository.deleteById(sl.getId())));
        userRepository.deleteById(userId);
    }

    @Test
    void testGetShoppingList() {
        ShoppingList shoppingListResponse = shoppingListService.getShoppingList(shoppingListId);
        assertEquals(shoppingListId, shoppingListResponse.getId());
        assertEquals(shoppingList.getTitle(), shoppingListResponse.getTitle());
        assertEquals(shoppingList.getStatus(), shoppingListResponse.getStatus());
        assertEquals(shoppingList.getExecutionDate(), shoppingListResponse.getExecutionDate());
    }

    @Test
    void testGetShoppingLists() {
        List<ShoppingList> shoppingListsResponse = shoppingListService.getShoppingLists();
        assertEquals(shoppingListRepository.findShoppingListByStatusInAndUser(
                Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED), user).get().size(),
                shoppingListsResponse.size());
    }

    @Test
    void testAddShoppingList() {
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setTitle("lista");
        shoppingList.setExecutionDate(LocalDateTime.parse("2023-04-08 12:30", dataFormatter));

        int countBefore = shoppingListRepository.findShoppingListByStatusInAndUser(
                Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED, EStatus.DELETED), user).get().size();

        shoppingListService.addShoppingList(shoppingList);

        assertEquals(countBefore+1,shoppingListRepository.findShoppingListByStatusInAndUser(
                Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED, EStatus.DELETED), user).get().size());
    }

    @Test
    void testUpdateShoppingList() {

        String newTitle = "newTitle";
        LocalDateTime newExecutionDate = LocalDateTime.parse("2023-04-10 12:30", dataFormatter);

        ShoppingListDto shoppingListDto = new ShoppingListDto();
        shoppingListDto.setStatus(statusAccomplished);
        shoppingListDto.setTitle(newTitle);
        shoppingListDto.setExecutionDate(newExecutionDate);

        shoppingListService.updateShoppingList(shoppingListId, shoppingListDto);
        shoppingListRepository.flush();

        Optional<ShoppingList> shoppingList = shoppingListRepository.findById(shoppingListId);

        assertNotNull(shoppingList);
        assertEquals(newTitle, shoppingList.get().getTitle());
        assertEquals(statusAccomplished, shoppingList.get().getStatus());
        assertEquals(newExecutionDate, shoppingList.get().getExecutionDate());
    }

    @Test
    void testDeleteShoppingList() {

        shoppingListService.deleteShoppingList(shoppingListId);
        shoppingListRepository.flush();

        Optional<ShoppingList> shoppingList = shoppingListRepository.findById(shoppingListId);

        assertNotNull(shoppingList);
        assertNotNull(shoppingList.get().getDeleteDate());
        assertEquals(statusDeleted, shoppingList.get().getStatus());
    }

}
