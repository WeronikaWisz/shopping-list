package com.shoppinglist.shoppinglistapp.dtos;

import com.shoppinglist.shoppinglistapp.enums.EStatus;
import com.shoppinglist.shoppinglistapp.enums.EUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class ShoppingItemDto implements Serializable {
    private Long id;
    private String name;
    private Double quantity;
    private EUnit unit;
    private EStatus status;
    private byte[] image;
    private Long shoppingListId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingItemDto that = (ShoppingItemDto) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name)
                && Objects.equals(quantity, that.quantity) && unit == that.unit
                && status == that.status && Arrays.equals(image, that.image)
                && Objects.equals(shoppingListId, that.shoppingListId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, name, quantity, unit, status, shoppingListId);
        result = 31 * result + Arrays.hashCode(image);
        return result;
    }
}
