package com.shoppinglist.shoppinglistapp.repositories;

import com.shoppinglist.shoppinglistapp.enums.EStatus;
import com.shoppinglist.shoppinglistapp.models.ShoppingList;
import com.shoppinglist.shoppinglistapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

    Optional<List<ShoppingList>> findShoppingListByStatusInAndUser(List<EStatus> statuses, User user);

}
