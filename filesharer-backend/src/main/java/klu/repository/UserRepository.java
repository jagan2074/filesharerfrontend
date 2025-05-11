package klu.repository; // This should match your package structure

import klu.model.User; // <<--- IMPORTANT: Import your User entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Optional for JpaRepository, but good practice

import java.util.Optional;

@Repository // Tells Spring that this is a repository bean
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository<User, Long> means:
    // - This repository works with 'User' entities.
    // - The ID type of the 'User' entity is 'Long'.

    // Spring Data JPA will automatically generate the SQL queries for these methods
    // based on their names.

    // Method to find a user by their username.
    // Returns an Optional, which can contain a User or be empty if not found.
    Optional<User> findByUsername(String username);

    // Method to find a user by their email.
    Optional<User> findByEmail(String email);

    // Method to check if a user with a given username already exists.
    boolean existsByUsername(String username);

    // Method to check if a user with a given email already exists.
    boolean existsByEmail(String email);
}