package klu.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// Add SLF4J imports for logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(JwtRequestFilter.class); // Added logger

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) { // Only proceed if JWT is present
                String username = null;
                try {
                    username = jwtUtil.extractUsername(jwt);
                } catch (io.jsonwebtoken.ExpiredJwtException e) {
                    slf4jLogger.warn("JWT token has expired: {}", e.getMessage());
                } catch (io.jsonwebtoken.JwtException e) { // Catch other JWT parsing/validation errors
                    slf4jLogger.warn("Invalid JWT token: {}", e.getMessage());
                }


                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        slf4jLogger.debug("JWT token validated and security context set for user: {}", username); // Debug log
                    } else {
                        slf4jLogger.warn("JWT token validation failed for user: {}", username);
                    }
                }
            }
        } catch (Exception e) {
            // This catch block is broad. More specific catches for JWT exceptions are better.
            // The refined try-catch for username extraction above is more targeted.
            slf4jLogger.error("Cannot set user authentication: {}", e.getMessage(), e); // Log stack trace for unexpected errors
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}