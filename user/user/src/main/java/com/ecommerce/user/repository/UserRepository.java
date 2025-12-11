package com.ecommerce.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ecommerce.user.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
}
