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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingItemRepository shoppingItemRepository;
    private final UserRepository userRepository;

    @Autowired
    public ShoppingListService(ShoppingListRepository shoppingListRepository, UserRepository userRepository,
                               ShoppingItemRepository shoppingItemRepository) {
        this.shoppingListRepository = shoppingListRepository;
        this.shoppingItemRepository = shoppingItemRepository;
        this.userRepository = userRepository;
    }

    public List<ShoppingList> getShoppingLists(){
        Optional<List<ShoppingList>> shoppingLists = shoppingListRepository.findShoppingListByStatusInAndUser(
                Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED), getCurrentUser());
        return shoppingLists.orElse(Collections.emptyList()).stream()
                .sorted(Comparator.comparing(ShoppingList::getExecutionDate)).collect(Collectors.toList());
    }

    @Transactional
    public void addShoppingList(ShoppingList shoppingList){
        shoppingList.setStatus(EStatus.WAITING);
        shoppingList.setUser(getCurrentUser());
        shoppingList.setCreationDate(LocalDateTime.now());
        shoppingListRepository.save(shoppingList);
    }

    @Transactional
    public void updateShoppingList(Long id, ShoppingListDto shoppingListDto){
        ShoppingList shoppingList = getShoppingList(id);

        if(!shoppingList.getUser().getUsername().equals(getCurrentUser().getUsername())){
            throw new ApiForbiddenException("exception.shoppingListNotBelongToUser");
        }

        if(shoppingList.getStatus().equals(EStatus.DELETED)){
            throw new ApiExpectationFailedException("exception.cannotEditDeletedShoppingList");
        }

        if(shoppingListDto.getExecutionDate() != null) {
            shoppingList.setExecutionDate(shoppingListDto.getExecutionDate());
        }
        if(shoppingListDto.getTitle() != null) {
            shoppingList.setTitle(shoppingListDto.getTitle());
        }
        if(shoppingListDto.getStatus() != null){
            EStatus newStatus = shoppingListDto.getStatus();
            shoppingList.setStatus(newStatus);
            if(newStatus.equals(EStatus.ACCOMPLISHED)){
                setAllItemsAccomplished(shoppingList);
            }
            if(newStatus.equals(EStatus.WAITING)){
                setAllItemsWaiting(shoppingList);
            }
        }
        shoppingList.setUpdateDate(LocalDateTime.now());
    }

    @Transactional
    public void deleteShoppingList(Long id){
        ShoppingList shoppingList = getShoppingList(id);

        if(!shoppingList.getUser().getUsername().equals(getCurrentUser().getUsername())){
            throw new ApiForbiddenException("exception.shoppingListNotBelongToUser");
        }

        shoppingList.setStatus(EStatus.DELETED);
        shoppingList.setDeleteDate(LocalDateTime.now());
    }

    public ShoppingList getShoppingList(Long id){
        return shoppingListRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("exception.shoppingListNotFound")
        );
    }

    private void setAllItemsAccomplished(ShoppingList shoppingList){
        List<ShoppingItem> shoppingItems = shoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(shoppingList,
                        Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED))
                .orElse(Collections.emptyList());
        shoppingItems.forEach(shoppingItem -> shoppingItem.setStatus(EStatus.ACCOMPLISHED));
    }

    private void setAllItemsWaiting(ShoppingList shoppingList){
        List<ShoppingItem> shoppingItems = shoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(shoppingList,
                        Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED))
                .orElse(Collections.emptyList());
        shoppingItems.forEach(shoppingItem -> shoppingItem.setStatus(EStatus.WAITING));
    }

    private User getCurrentUser(){
        UserDetailsI userDetails = (UserDetailsI) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String username = userDetails.getUsername();
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Cannot found user"));
    }
}
