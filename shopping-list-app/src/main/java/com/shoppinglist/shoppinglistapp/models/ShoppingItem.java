package com.shoppinglist.shoppinglistapp.models;

import com.shoppinglist.shoppinglistapp.enums.EStatus;
import com.shoppinglist.shoppinglistapp.enums.EUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class ShoppingItem {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String name;
    private Double quantity;
    @Enumerated
    @Column(columnDefinition = "smallint")
    private EUnit unit;
    @Lob
    private byte[] image;
    @Enumerated
    @Column(columnDefinition = "smallint")
    private EStatus status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", nullable=false)
    private ShoppingList shoppingList;

    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
    private LocalDateTime deleteDate;
}
