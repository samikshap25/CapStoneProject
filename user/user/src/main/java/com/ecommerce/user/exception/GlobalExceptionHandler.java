package com.ecommerce.user.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.jsonwebtoken.security.SignatureException;

@ControllerAdvice
public class GlobalExceptionHandler {

	Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/*
	 * Whenever a RuntimeException is thrown in Controller, this method gets called
	 */
	@ExceptionHandler(RuntimeException.class)  // <- REMOVED 'exception ='
	public ResponseEntity<?> handleRuntime(RuntimeException e) {
		logger.info(e.getMessage());
		Map<String, String> map = new HashMap<>();
		map.put("msg", e.getMessage());
		logger.error(e.getMessage(), e);
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(map);
	}

	/*
	 * Whenever a Custom ResourceNotFoundException is thrown in Controller,
	 * this method gets called
	 */
	@ExceptionHandler(ResourceNotFoundException.class)  // <- REMOVED 'exception ='
	public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException e) {
		Map<String, String> map = new HashMap<>();
		map.put("msg", e.getMessage());
		logger.error(e.getMessage(), e);
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(map);
	}

	/*
	 * Whenever any Unforeseen Exception is thrown in Controller,
	 * this method gets called
	 */
	@ExceptionHandler(Exception.class)  // <- REMOVED 'exception ='
	public ResponseEntity<?> handleException(Exception e) {
		Map<String, String> map = new HashMap<>();
		map.put("msg", e.getMessage());
		logger.error(e.getMessage(), e);
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)  // <- Changed from NOT_FOUND
				.body(map);
	}

	/*
	 * Whenever a token is invalid, this method gets called
	 */
	@ExceptionHandler(SignatureException.class)  // <- REMOVED 'exception ='
	public ResponseEntity<?> handleSignatureException(SignatureException e) {
		Map<String, String> map = new HashMap<>();
		map.put("msg", "Invalid JWT token: " + e.getMessage());
		logger.error(e.getMessage(), e);
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(map);
	}
}