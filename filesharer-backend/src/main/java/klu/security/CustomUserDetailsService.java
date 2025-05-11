package klu.security; // Or your.basepackage.security

 import klu.model.User;
 import klu.repository.UserRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.stereotype.Service;

 import java.util.ArrayList; // For creating an empty list of authorities

 @Service // Marks this as a Spring service component
 public class CustomUserDetailsService implements UserDetailsService {

     @Autowired
     private UserRepository userRepository; // Injects our UserRepository

     @Override
     public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
         // Try to find the user by username OR email
         User user = userRepository.findByUsername(usernameOrEmail)
                 .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                         .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail)));

         // Creates a Spring Security UserDetails object.
         // For this simple version, we are not using roles/authorities, so we pass an empty list.
         return new org.springframework.security.core.userdetails.User(
                 user.getUsername(), // The username that Spring Security will use
                 user.getPassword(),   // The hashed password from our database
                 new ArrayList<>()    // Empty list of authorities (roles)
         );
     }

     // Optional helper method if you need to load a user by ID directly for UserDetails
     public UserDetails loadUserById(Long id) {
         User user = userRepository.findById(id).orElseThrow(
                 () -> new UsernameNotFoundException("User not found with id : " + id)
         );
         return new org.springframework.security.core.userdetails.User(
                 user.getUsername(), user.getPassword(), new ArrayList<>());
     }
 }