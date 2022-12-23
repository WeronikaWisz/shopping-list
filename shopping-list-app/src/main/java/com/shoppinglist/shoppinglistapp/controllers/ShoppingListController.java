package com.shoppinglist.shoppinglistapp.controllers;

import com.shoppinglist.shoppinglistapp.dtos.ShoppingListDto;
import com.shoppinglist.shoppinglistapp.dtos.auth.MessageResponse;
import com.shoppinglist.shoppinglistapp.models.ShoppingList;
import com.shoppinglist.shoppinglistapp.services.ShoppingListService;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/shopping-list")
@CrossOrigin("http://localhost:4200")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private MessageSource messageSource;
    private ModelMapper modelMapper;

    public ShoppingListController(ShoppingListService shoppingListService, MessageSource messageSource,
                                  ModelMapper modelMapper) {
        this.shoppingListService = shoppingListService;
        this.messageSource = messageSource;
        this.modelMapper = modelMapper;
        modelMapper.addMappings(new PropertyMap<ShoppingListDto, ShoppingList>() {
            @Override
            protected void configure() {
                skip(destination.getId());
            }
        });
    }

    @GetMapping(path = "/shopping-list")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getShoppingLists() {
        List<ShoppingListDto> shoppingListsDto = shoppingListService.getShoppingLists().stream()
                .map(this::mapShoppingListToShoppingListDto).collect(Collectors.toList());
        return ResponseEntity.ok(shoppingListsDto);
    }

    @PostMapping(path = "/shopping-list")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addShoppingList(@RequestBody ShoppingListDto shoppingListDto) {
        shoppingListService.addShoppingList(mapShoppingListDtoToShoppingList(shoppingListDto));
        return ResponseEntity.ok(new MessageResponse(messageSource.getMessage(
                "success.addShoppingList", null, LocaleContextHolder.getLocale())));
    }

    @PutMapping(path = "/shopping-list/{shoppingListId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateShoppingList(@PathVariable(value = "shoppingListId") Long shoppingListId,
                                                @RequestBody ShoppingListDto shoppingListDto) {
        shoppingListService.updateShoppingList(shoppingListId, shoppingListDto);
        return ResponseEntity.ok(new MessageResponse(messageSource.getMessage(
                "success.updateShoppingList", null, LocaleContextHolder.getLocale())));
    }

    @DeleteMapping(path = "/shopping-list/{shoppingListId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteShoppingList(@PathVariable(value = "shoppingListId") Long shoppingListId) {
        shoppingListService.deleteShoppingList(shoppingListId);
        return ResponseEntity.ok(new MessageResponse(messageSource.getMessage(
                "success.deleteShoppingList", null, LocaleContextHolder.getLocale())));
    }

    private ShoppingList mapShoppingListDtoToShoppingList(ShoppingListDto shoppingListDto){
        return modelMapper.map(shoppingListDto, ShoppingList.class);
    }

    private ShoppingListDto mapShoppingListToShoppingListDto(ShoppingList shoppingList){
        return modelMapper.map(shoppingList, ShoppingListDto.class);
    }

}
