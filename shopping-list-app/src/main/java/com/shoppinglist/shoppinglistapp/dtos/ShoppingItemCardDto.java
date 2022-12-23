package com.shoppinglist.shoppinglistapp.dtos;

import com.shoppinglist.shoppinglistapp.enums.EStatus;
import com.shoppinglist.shoppinglistapp.enums.EUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class ShoppingItemCardDto implements Serializable {
    private Long id;
    private String name;
    private Double quantity;
    private EUnit unit;
    private EStatus status;
    private Long shoppingListId;
    private boolean hasImage;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingItemCardDto that = (ShoppingItemCardDto) o;
        return hasImage == that.hasImage && Objects.equals(id, that.id)
                && Objects.equals(name, that.name) && Objects.equals(quantity, that.quantity)
                && unit == that.unit && status == that.status && Objects.equals(shoppingListId, that.shoppingListId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, quantity, unit, status, shoppingListId, hasImage);
    }
}
