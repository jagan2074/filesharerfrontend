 package klu.security; // Or your.basepackage.security

 import io.jsonwebtoken.Claims;
 import io.jsonwebtoken.Jwts;
 import io.jsonwebtoken.SignatureAlgorithm;
 import io.jsonwebtoken.security.Keys;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Component;

 import javax.crypto.SecretKey;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.function.Function;

 @Component // Marks this as a Spring-managed component
 public class JwtUtil {

     @Value("${jwt.secret}") // Injects the secret key from application.properties
     private String secretKeyString;

     @Value("${jwt.expiration.ms}") // Injects the expiration time from application.properties
     private long expirationMs;

     // Generates a secure signing key from the secret string
     private SecretKey getSigningKey() {
         // Ensure the secret key is long enough for HS256 (at least 32 bytes / 256 bits)
         byte[] keyBytes = secretKeyString.getBytes();
         if (keyBytes.length < 32) {
             throw new IllegalArgumentException("JWT secret key must be at least 256 bits (32 bytes) long for HS256. Current length: " + keyBytes.length + " bytes.");
         }
         return Keys.hmacShaKeyFor(keyBytes);
     }

     // Extracts the username (subject) from the token
     public String extractUsername(String token) {
         return extractClaim(token, Claims::getSubject);
     }

     // Extracts the expiration date from the token
     public Date extractExpiration(String token) {
         return extractClaim(token, Claims::getExpiration);
     }

     // Generic method to extract a specific claim from the token
     public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
         final Claims claims = extractAllClaims(token);
         return claimsResolver.apply(claims);
     }

     // Parses the token and retrieves all claims
     private Claims extractAllClaims(String token) {
         return Jwts.parser()
                 .verifyWith(getSigningKey()) // Verifies the token's signature
                 .build()
                 .parseSignedClaims(token)
                 .getPayload();
     }

     // Checks if the token has expired
     private Boolean isTokenExpired(String token) {
         return extractExpiration(token).before(new Date());
     }

     // Generates a new JWT for a given UserDetails object
     public String generateToken(UserDetails userDetails) {
         Map<String, Object> claims = new HashMap<>();
         // You can add custom claims here if needed, e.g., roles
         // claims.put("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
         return createToken(claims, userDetails.getUsername());
     }

     // Helper method to create the token
     private String createToken(Map<String, Object> claims, String subject) {
         return Jwts.builder()
                 .claims(claims)
                 .subject(subject) // Sets the 'subject' of the token (usually the username)
                 .issuedAt(new Date(System.currentTimeMillis())) // Sets the token issuance time
                 .expiration(new Date(System.currentTimeMillis() + expirationMs)) // Sets the expiration time
                 .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Signs the token with HS256 algorithm
                 .compact();
     }

     // Validates the token against UserDetails (checks username and expiration)
     public Boolean validateToken(String token, UserDetails userDetails) {
         final String username = extractUsername(token);
         return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
     }
 }