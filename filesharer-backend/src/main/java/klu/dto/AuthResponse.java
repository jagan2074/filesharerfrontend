package klu.dto;

import java.util.Objects;

public class AuthResponse {
    private String token;
    private String username;

    // No-argument constructor
    public AuthResponse() {
    }

    // Constructor with all fields
    public AuthResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthResponse that = (AuthResponse) o;
        return Objects.equals(token, that.token) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, username);
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='[PROTECTED]'" + // Or a portion of it for debugging if needed
                ", username='" + username + '\'' +
                '}';
    }
}