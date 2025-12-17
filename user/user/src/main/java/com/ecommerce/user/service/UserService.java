package com.ecommerce.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;

@Service
public class UserService {

	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public User signUp(User user) {
		// Check if user already exists
		User existingUser = userRepository.findByUsername(user.getUsername());
		if (existingUser != null) {
			throw new RuntimeException("Username already exists");
		}
		
		// Encrypt the plain text password
		String plainPassword = user.getPassword();
		String encodedPassword = passwordEncoder.encode(plainPassword);
		user.setPassword(encodedPassword);

		// Save User in DB
		return userRepository.save(user);
	}

	public User getUserInfo(String username) {
		User user = userRepository.findByUsername(username);
		if (user == null) {
			throw new ResourceNotFoundException("User not found with username: " + username);
		}
		
		// Return user without password for security
		user.setPassword(null);
		return user;
	}
	
	public User getUserById(int id) {
		return userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
	}
	
	public User updateUser(int id, User userDetails) {
		User user = getUserById(id);
		
		if (userDetails.getUsername() != null) {
			user.setUsername(userDetails.getUsername());
		}
		
		if (userDetails.getPassword() != null) {
			String encodedPassword = passwordEncoder.encode(userDetails.getPassword());
			user.setPassword(encodedPassword);
		}
		
		if (userDetails.getRole() != null) {
			user.setRole(userDetails.getRole());
		}
		
		return userRepository.save(user);
	}
}