
 package klu.service; // Or your.basepackage.service

 import klu.dto.AuthResponse;
 import klu.dto.LoginRequest;
 import klu.dto.RegisterRequest;
 import klu.model.User;
 import klu.repository.UserRepository;
 import klu.security.JwtUtil; // We created this earlier
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.crypto.password.PasswordEncoder; // Will be injected
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional; // Optional for read-only methods

 @Service // Marks this as a Spring service component
 public class AuthService {

     @Autowired
     private UserRepository userRepository;

     @Autowired
     private PasswordEncoder passwordEncoder; // We'll configure this bean in SecurityConfig

     @Autowired
     private AuthenticationManager authenticationManager; // We'll configure this bean in SecurityConfig

     @Autowired
     private JwtUtil jwtUtil;

     @Transactional // Good practice for methods that modify data
     public User registerUser(RegisterRequest registerRequest) {
         // Check if username already exists
         if (userRepository.existsByUsername(registerRequest.getUsername())) {
             throw new RuntimeException("Error: Username is already taken!"); // Consider a custom exception
         }

         // Check if email already exists
         if (userRepository.existsByEmail(registerRequest.getEmail())) {
             throw new RuntimeException("Error: Email is already in use!"); // Consider a custom exception
         }

         // Create new user's account
         User user = new User();
         user.setUsername(registerRequest.getUsername());
         user.setEmail(registerRequest.getEmail());
         // Encode the password before saving
         user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
         // Roles can be set here if you implement them

         return userRepository.save(user);
     }

     public AuthResponse loginUser(LoginRequest loginRequest) {
         // Authenticate the user using Spring Security's AuthenticationManager
         Authentication authentication = authenticationManager.authenticate(
                 new UsernamePasswordAuthenticationToken(
                         loginRequest.getUsernameOrEmail(),
                         loginRequest.getPassword()
                 )
         );

         // If authentication is successful, set the authentication in the SecurityContext
         SecurityContextHolder.getContext().setAuthentication(authentication);

         // Get the UserDetails object from the authentication principal
         UserDetails userDetails = (UserDetails) authentication.getPrincipal();

         // Generate JWT token
         String jwt = jwtUtil.generateToken(userDetails);

         // Return the token and username in the AuthResponse
         return new AuthResponse(jwt, userDetails.getUsername());
     }
 }