package com.shoppinglist.shoppinglistapp.repositories;

import com.shoppinglist.shoppinglistapp.enums.EStatus;
import com.shoppinglist.shoppinglistapp.models.ShoppingItem;
import com.shoppinglist.shoppinglistapp.models.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {

    Optional<List<ShoppingItem>> findShoppingItemByShoppingListAndStatusIn(ShoppingList shoppingList, List<EStatus> statuses);
}
