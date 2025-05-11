package klu.controller; // Or your.basepackage.controller

import klu.dto.AuthResponse;
import klu.dto.LoginRequest;
import klu.dto.RegisterRequest;
import klu.service.AuthService; // Your AuthService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Marks this as a REST controller, combines @Controller and @ResponseBody
@RequestMapping("/api/auth") // Base path for all endpoints in this controller
// @CrossOrigin(origins = "http://localhost:3000") // Already configured globally in SecurityConfig
public class AuthController {

    @Autowired
    private AuthService authService; // Injects your AuthService

    @PostMapping("/register") // Maps HTTP POST requests to /api/auth/register
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        // @RequestBody tells Spring to convert the JSON from the request body to a RegisterRequest object
        try {
            authService.registerUser(registerRequest);
            return ResponseEntity.ok("User registered successfully!"); // 200 OK
        } catch (RuntimeException e) {
            // Simple error handling for now
            return ResponseEntity.badRequest().body(e.getMessage()); // 400 Bad Request
        }
    }

    @PostMapping("/login") // Maps HTTP POST requests to /api/auth/login
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.loginUser(loginRequest);
            return ResponseEntity.ok(authResponse); // 200 OK with AuthResponse (token) in body
        } catch (Exception e) {
            // Catching generic Exception for authentication failures (e.g., BadCredentialsException)
            // Spring Security will throw specific exceptions, but this is a simple catch-all for now.
            return ResponseEntity.status(401).body("Invalid username/email or password"); // 401 Unauthorized
        }
    }
}