package com.shoppinglist.shoppinglistapp.services;

import com.shoppinglist.shoppinglistapp.dtos.ShoppingItemDto;
import com.shoppinglist.shoppinglistapp.enums.EStatus;
import com.shoppinglist.shoppinglistapp.enums.EUnit;
import com.shoppinglist.shoppinglistapp.exception.ApiNotFoundException;
import com.shoppinglist.shoppinglistapp.models.ShoppingItem;
import com.shoppinglist.shoppinglistapp.models.ShoppingList;
import com.shoppinglist.shoppinglistapp.models.User;
import com.shoppinglist.shoppinglistapp.repositories.RoleRepository;
import com.shoppinglist.shoppinglistapp.repositories.ShoppingItemRepository;
import com.shoppinglist.shoppinglistapp.repositories.ShoppingListRepository;
import com.shoppinglist.shoppinglistapp.repositories.UserRepository;
import com.shoppinglist.shoppinglistapp.security.WebSecurityConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Import(WebSecurityConfig.class)
@WithUserDetails("username")
public class ShoppingItemServiceIntegrationTest {


    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private ShoppingItemRepository shoppingItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ShoppingItemService shoppingItemService;

    private Long userId;
    User user = new User();

    ShoppingList shoppingList = new ShoppingList();
    private Long shoppingListId;
    private final EStatus statusWaiting = EStatus.WAITING;
    private final EStatus statusAccomplished = EStatus.ACCOMPLISHED;
    private final EStatus statusDeleted = EStatus.DELETED;
    private final DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    ShoppingItem shoppingItem = new ShoppingItem();
    private Long shoppingItemId;

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

        shoppingItem.setShoppingList(savedShoppingList);
        shoppingItem.setName("name");
        shoppingItem.setQuantity(1d);
        shoppingItem.setUnit(EUnit.PIECES);
        shoppingItem.setStatus(statusWaiting);
        shoppingItem.setCreationDate(LocalDateTime.now());
        ShoppingItem savedShoppingItem = shoppingItemRepository.save(shoppingItem);
        shoppingItemId = savedShoppingItem.getId();
    }

    @AfterAll
    public void afterAll() {
        Optional<List<ShoppingItem>> shoppingItems = shoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(
                shoppingList, Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED, EStatus.DELETED));
        shoppingItems.ifPresent(lists -> lists.forEach(si -> shoppingItemRepository.deleteById(si.getId())));
        Optional<List<ShoppingList>> shoppingLists = shoppingListRepository.findShoppingListByStatusInAndUser(
                Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED, EStatus.DELETED), user);
        shoppingLists.ifPresent(lists -> lists.forEach(sl -> shoppingListRepository.deleteById(sl.getId())));
        userRepository.deleteById(userId);
    }

    @Test
    void testGetShoppingItem() {
        ShoppingItem responseShoppingItem = shoppingItemService.getShoppingItem(shoppingItemId);
        assertEquals(shoppingItem.getName(), responseShoppingItem.getName());
        assertEquals(shoppingItem.getStatus(),responseShoppingItem.getStatus());
        assertEquals(shoppingItem.getQuantity(), responseShoppingItem.getQuantity());
        assertEquals(shoppingItem.getUnit(), shoppingItem.getUnit());
    }

    @Test
    void testGetShoppingItemNotFound() {
        assertThrows(ApiNotFoundException.class, () -> shoppingItemService.getShoppingItem(shoppingItemId+2));
    }

    @Test
    void testGetShoppingItems() {
        List<ShoppingItem> shoppingItemsResponse = shoppingItemService.getShoppingItems(shoppingListId);
        assertEquals(shoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(shoppingList,
                        Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED)).get().size(),
                shoppingItemsResponse.size());
    }

    @Test
    void testGetShoppingItemsListNotFound() {
        assertThrows(ApiNotFoundException.class, () -> shoppingItemService.getShoppingItems(shoppingListId+1));
    }

    @Test
    void testAddShoppingItem() {
        ShoppingItem shoppingItem = new ShoppingItem();
        shoppingItem.setName("name");
        shoppingItem.setCreationDate(LocalDateTime.now());

        int countBefore = shoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(
                shoppingList, Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED, EStatus.DELETED)).get().size();

        shoppingItemService.addShoppingItem(shoppingItem, shoppingListId);
        shoppingItemRepository.flush();

        assertEquals(countBefore+1,shoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(
                shoppingList, Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED, EStatus.DELETED)).get().size());
    }

    @Test
    void testUpdateShoppingItem() {

        String newName = "newName";
        EUnit newUnit = EUnit.LITERS;
        Double newQuantity = 2d;

        ShoppingItemDto shoppingItemDto = new ShoppingItemDto();
        shoppingItemDto.setStatus(statusAccomplished);
        shoppingItemDto.setName(newName);
        shoppingItemDto.setUnit(newUnit);
        shoppingItemDto.setQuantity(newQuantity);

        shoppingItemService.updateShoppingItem(shoppingItemId, shoppingItemDto);
        shoppingItemRepository.flush();

        Optional<ShoppingItem> shoppingItem = shoppingItemRepository.findById(shoppingItemId);

        assertNotNull(shoppingItem);
        assertEquals(newName, shoppingItem.get().getName());
        assertEquals(newUnit, shoppingItem.get().getUnit());
        assertEquals(newQuantity, shoppingItem.get().getQuantity());
        assertEquals(statusAccomplished, shoppingItem.get().getStatus());
    }

    @Test
    void testDeleteShoppingItem() {
        shoppingItemService.deleteShoppingItem(shoppingItemId);
        shoppingItemRepository.flush();

        Optional<ShoppingItem> shoppingItem = shoppingItemRepository.findById(shoppingItemId);

        assertNotNull(shoppingItem);
        assertNotNull(shoppingItem.get().getDeleteDate());
        assertEquals(statusDeleted, shoppingItem.get().getStatus());
    }


}
