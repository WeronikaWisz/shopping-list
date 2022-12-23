package com.shoppinglist.shoppinglistapp.models;

import com.shoppinglist.shoppinglistapp.enums.EStatus;
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
public class ShoppingList {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String title;
    private LocalDateTime executionDate;
    @Enumerated
    @Column(columnDefinition = "smallint")
    private EStatus status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable=false)
    private User user;

    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
    private LocalDateTime deleteDate;
}
