package com.shoppinglist.shoppinglistapp.models;

import com.shoppinglist.shoppinglistapp.enums.ERole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name;
}
