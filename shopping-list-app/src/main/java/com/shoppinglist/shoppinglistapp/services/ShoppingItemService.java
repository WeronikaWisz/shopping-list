package com.shoppinglist.shoppinglistapp.services;

import com.shoppinglist.shoppinglistapp.dtos.ShoppingItemDto;
import com.shoppinglist.shoppinglistapp.enums.EStatus;
import com.shoppinglist.shoppinglistapp.exception.ApiExpectationFailedException;
import com.shoppinglist.shoppinglistapp.exception.ApiForbiddenException;
import com.shoppinglist.shoppinglistapp.exception.ApiNotFoundException;
import com.shoppinglist.shoppinglistapp.helpers.ImageHelper;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShoppingItemService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final UserRepository userRepository;

    @Autowired
    public ShoppingItemService(ShoppingItemRepository shoppingItemRepository, UserRepository userRepository,
                               ShoppingListRepository shoppingListRepository) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.userRepository = userRepository;
        this.shoppingListRepository = shoppingListRepository;
    }

    @Transactional
    public List<ShoppingItem> getShoppingItems(Long shoppingListId){
        Optional<List<ShoppingItem>> shoppingItems = shoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(
                getShoppingList(shoppingListId), Arrays.asList(EStatus.WAITING, EStatus.ACCOMPLISHED));
        return shoppingItems.orElse(Collections.emptyList()).stream()
                .sorted(Comparator.comparing(ShoppingItem::getCreationDate).reversed()).collect(Collectors.toList());
    }

    @Transactional
    public void addShoppingItem(MultipartFile image, ShoppingItem shoppingItem, Long shoppingListId) throws IOException {
        shoppingItem.setImage(ImageHelper.compressBytes(image.getBytes()));
        addShoppingItemInfo(shoppingItem, shoppingListId);
    }

    @Transactional
    public void addShoppingItem(ShoppingItem shoppingItem, Long shoppingListId){
        addShoppingItemInfo(shoppingItem, shoppingListId);
    }

    private void addShoppingItemInfo(ShoppingItem shoppingItem, Long shoppingListId){
        ShoppingList shoppingList = getShoppingList(shoppingListId);

        if(!shoppingList.getUser().getUsername().equals(getCurrentUser().getUsername())){
            throw new ApiForbiddenException("exception.shoppingListNotBelongToUser");
        }

        shoppingItem.setCreationDate(LocalDateTime.now());
        shoppingItem.setStatus(EStatus.WAITING);
        shoppingItem.setShoppingList(shoppingList);
        shoppingItemRepository.save(shoppingItem);
    }

    @Transactional
    public void updateShoppingItem(Long id, MultipartFile image, ShoppingItemDto shoppingItemDto) throws IOException {
        ShoppingItem shoppingItem = updateShoppingItemInfo(id, shoppingItemDto);
        shoppingItem.setImage(ImageHelper.compressBytes(image.getBytes()));
    }

    @Transactional
    public void updateShoppingItem(Long id, ShoppingItemDto shoppingItemDto){
        updateShoppingItemInfo(id, shoppingItemDto);
    }

    private ShoppingItem updateShoppingItemInfo(Long id, ShoppingItemDto shoppingItemDto){
        ShoppingItem shoppingItem = getShoppingItem(id);
        User user = getCurrentUser();

        if(!shoppingItem.getShoppingList().getUser().getUsername().equals(user.getUsername())){
            throw new ApiForbiddenException("exception.shoppingListNotBelongToUser");
        }

        if(shoppingItem.getStatus().equals(EStatus.DELETED)){
            throw new ApiExpectationFailedException("exception.cannotEditDeletedShoppingItem");
        }
        if (shoppingItemDto.getName() != null) {
            shoppingItem.setName(shoppingItemDto.getName());
        }
        if(shoppingItemDto.getQuantity() != null ) {
            shoppingItem.setQuantity(shoppingItemDto.getQuantity());
        }
        if(shoppingItemDto.getUnit() != null) {
            shoppingItem.setUnit(shoppingItemDto.getUnit());
        }
        if(shoppingItemDto.getStatus() != null) {
            shoppingItem.setStatus(shoppingItemDto.getStatus());
            setShoppingListAccomplishedOrWaiting(shoppingItem.getShoppingList());
        }
        shoppingItem.setUpdateDate(LocalDateTime.now());

        return shoppingItem;
    }

    @Transactional
    public void deleteShoppingItem(Long id){
        ShoppingItem shoppingItem = getShoppingItem(id);

        if(!shoppingItem.getShoppingList().getUser().getUsername().equals(getCurrentUser().getUsername())){
            throw new ApiForbiddenException("exception.shoppingItemNotBelongToUser");
        }

        shoppingItem.setStatus(EStatus.DELETED);
        shoppingItem.setDeleteDate(LocalDateTime.now());
    }

    public ShoppingItem getShoppingItem(Long id){
        return shoppingItemRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("exception.shoppingItemNotFound")
        );
    }

    private ShoppingList getShoppingList(Long id){
        return shoppingListRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("exception.shoppingListNotFound")
        );
    }

    private void setShoppingListAccomplishedOrWaiting(ShoppingList shoppingList){
        List<ShoppingItem> shoppingItemsWaiting = shoppingItemRepository.findShoppingItemByShoppingListAndStatusIn(
                shoppingList, List.of(EStatus.WAITING))
                .orElse(Collections.emptyList());
        if(shoppingItemsWaiting.isEmpty()){
            shoppingList.setStatus(EStatus.ACCOMPLISHED);
        } else {
            shoppingList.setStatus(EStatus.WAITING);
        }
        shoppingListRepository.save(shoppingList);
    }

    private User getCurrentUser(){
        UserDetailsI userDetails = (UserDetailsI) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String username = userDetails.getUsername();
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Cannot found user"));
    }
}
