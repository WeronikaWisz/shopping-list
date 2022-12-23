package com.shoppinglist.shoppinglistapp.controllers;

import com.shoppinglist.shoppinglistapp.dtos.ImageInfoDto;
import com.shoppinglist.shoppinglistapp.dtos.ShoppingItemCardDto;
import com.shoppinglist.shoppinglistapp.dtos.ShoppingItemDto;
import com.shoppinglist.shoppinglistapp.dtos.auth.MessageResponse;
import com.shoppinglist.shoppinglistapp.helpers.ImageHelper;
import com.shoppinglist.shoppinglistapp.models.ShoppingItem;
import com.shoppinglist.shoppinglistapp.services.ShoppingItemService;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/shopping-item")
@CrossOrigin("http://localhost:4200")
public class ShoppingItemController {

    private final ShoppingItemService shoppingItemService;
    private MessageSource messageSource;
    private ModelMapper modelMapper;
    private static final Logger logger = LoggerFactory.getLogger(ShoppingItemController.class);

    public ShoppingItemController(ShoppingItemService shoppingItemService, MessageSource messageSource,
                                  ModelMapper modelMapper) {
        this.shoppingItemService = shoppingItemService;
        this.messageSource = messageSource;
        this.modelMapper = modelMapper;
        modelMapper.addMappings(new PropertyMap<ShoppingItemDto, ShoppingItem>() {
            @Override
            protected void configure() {
                skip(destination.getId());
            }
        });
    }

    @GetMapping(path = "/shopping-item/{shoppingListId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getShoppingItems(@PathVariable(value = "shoppingListId") Long shoppingListId) {
        List<ShoppingItemCardDto> shoppingItemsCardDto = shoppingItemService.getShoppingItems(shoppingListId).stream()
                .map(this::mapShoppingItemToShoppingItemCardDto).collect(Collectors.toList());
        return ResponseEntity.ok(shoppingItemsCardDto);
    }

    @PostMapping(path = "/shopping-item", consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addShoppingItem(@RequestPart(name="image", required=false) MultipartFile image,
                                     @RequestPart("info") ShoppingItemDto shoppingItemDto) {
        if(image!=null) {
            try {
                this.shoppingItemService.addShoppingItem(image, mapShoppingItemDtoToShoppingItem(shoppingItemDto),
                        shoppingItemDto.getShoppingListId());
            } catch (IOException e) {
                logger.error("Error getting bytes from file: " + e.getMessage());
                return ResponseEntity
                        .status(HttpStatus.EXPECTATION_FAILED)
                        .body(new MessageResponse(messageSource.getMessage(
                                "exception.cannotSaveFile", null, LocaleContextHolder.getLocale())));
            }
        } else {
            this.shoppingItemService.addShoppingItem(mapShoppingItemDtoToShoppingItem(shoppingItemDto),
                    shoppingItemDto.getShoppingListId());
        }
        return ResponseEntity.ok(new MessageResponse(messageSource.getMessage(
                "success.addShoppingItem", null, LocaleContextHolder.getLocale())));
    }

    @PutMapping(path = "/shopping-item/{id}", consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateShoppingItem(@RequestPart(name="image", required=false) MultipartFile image,
                                        @RequestPart("info") ShoppingItemDto shoppingItemDto,
                                        @PathVariable(value = "id") Long shoppingItemId) {
        if(image!=null) {
            try {
                this.shoppingItemService.updateShoppingItem(shoppingItemId, image, shoppingItemDto);
            } catch (IOException e) {
                logger.error("Error getting bytes from file: " + e.getMessage());
                return ResponseEntity
                        .status(HttpStatus.EXPECTATION_FAILED)
                        .body(new MessageResponse(messageSource.getMessage(
                                "exception.cannotSaveFile", null, LocaleContextHolder.getLocale())));
            }
        } else {
            this.shoppingItemService.updateShoppingItem(shoppingItemId, shoppingItemDto);
        }
        return ResponseEntity.ok(new MessageResponse(messageSource.getMessage(
                "success.updateShoppingItem", null, LocaleContextHolder.getLocale())));
    }

    @DeleteMapping(path = "/shopping-item/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteShoppingItem(@PathVariable(value = "id") Long shoppingItemId) {
        shoppingItemService.deleteShoppingItem(shoppingItemId);
        return ResponseEntity.ok(new MessageResponse(messageSource.getMessage(
                "success.deleteShoppingItem", null, LocaleContextHolder.getLocale())));
    }

    @GetMapping(path = "/shopping-item/{id}/image")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getShoppingItemImage(@PathVariable(value = "id") Long shoppingItemId) {
        ShoppingItem shoppingItem = shoppingItemService.getShoppingItem(shoppingItemId);
        ImageInfoDto imageInfoDto = new ImageInfoDto();
        imageInfoDto.setName(shoppingItem.getName());
        if(shoppingItem.getImage() != null) {
            imageInfoDto.setImage(ImageHelper.decompressBytes(shoppingItem.getImage()));
        } else {
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(new MessageResponse(messageSource.getMessage(
                            "exception.itemDoesNotHaveImage", null, LocaleContextHolder.getLocale())));
        }
        return ResponseEntity.ok(imageInfoDto);
    }

    private ShoppingItem mapShoppingItemDtoToShoppingItem(ShoppingItemDto shoppingItemDto){
        return modelMapper.map(shoppingItemDto, ShoppingItem.class);
    }

    private ShoppingItemCardDto mapShoppingItemToShoppingItemCardDto(ShoppingItem shoppingItem){
        ShoppingItemCardDto shoppingItemCardDto = modelMapper.map(shoppingItem, ShoppingItemCardDto.class);
        shoppingItemCardDto.setShoppingListId(shoppingItem.getShoppingList().getId());
        shoppingItemCardDto.setHasImage(shoppingItem.getImage() != null);
        return shoppingItemCardDto;
    }
}
