package com.ecommerce.user.dto;

public class ValidationResponse {

    private boolean valid;
    private String username;
    private Integer userId;
    private String role;

    // Constructors
    public ValidationResponse() {
    }

    public ValidationResponse(boolean valid, String username, Integer userId, String role) {
        this.valid = valid;
        this.username = username;
        this.userId = userId;
        this.role = role;
    }

    // Getters and Setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "ValidationResponse{" +
                "valid=" + valid +
                ", username='" + username + '\'' +
                ", userId=" + userId +
                ", role='" + role + '\'' +
                '}';
    }
}