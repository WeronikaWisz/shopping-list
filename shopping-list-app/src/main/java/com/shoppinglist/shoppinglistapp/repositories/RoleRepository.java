package com.shoppinglist.shoppinglistapp.repositories;

import com.shoppinglist.shoppinglistapp.enums.ERole;
import com.shoppinglist.shoppinglistapp.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
