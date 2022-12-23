package com.shoppinglist.shoppinglistapp.dtos;

import com.shoppinglist.shoppinglistapp.enums.EStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class ShoppingListDto implements Serializable {
    private Long id;
    private String title;
    private LocalDateTime executionDate;
    private EStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingListDto that = (ShoppingListDto) o;
        return Objects.equals(id, that.id) && Objects.equals(title, that.title)
                && Objects.equals(executionDate, that.executionDate) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, executionDate, status);
    }
}
