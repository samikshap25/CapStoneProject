package com.ecommerce.user.service;

import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired private UserRepository repo;
    @Autowired private PasswordEncoder encoder;

    public User register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);
    }

    public User getByUsername(String username) {
        return repo.findByUsername(username);
    }
}
