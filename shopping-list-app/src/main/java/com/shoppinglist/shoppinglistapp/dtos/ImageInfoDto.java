package com.shoppinglist.shoppinglistapp.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImageInfoDto {
    private String name;
    private byte[] image;
}
