package com.shoppinglist.shoppinglistapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ShoppingListAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShoppingListAppApplication.class, args);
	}

}
