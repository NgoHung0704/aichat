package ie.tcd.scss.aichat.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import ie.tcd.scss.aichat.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT Authentication Filter
 * Intercepts every HTTP request to validate JWT tokens and set up Spring Security authentication.
 * 
 * This filter:
 * 1. Extracts JWT token from Authorization header
 * 2. Validates the token signature and expiration
 * 3. Loads user details from database
 * 4. Sets authentication in Spring Security context
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
<<<<<<< HEAD
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Extract Authorization header
        final String authorizationHeader = request.getHeader("Authorization");
=======
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Extract Study-Auth header (custom header to avoid conflicts with Coder proxy)
        final String authorizationHeader = request.getHeader("Study-Auth");
>>>>>>> 632baa1304b9f5bf2c200e5a9c5e9e0e40e04c94

        String username = null;
        String jwtToken = null;

        // Check if header contains Bearer token
<<<<<<< HEAD
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7); // Remove "Bearer " prefix
=======
        if (authorizationHeader != null) {
            jwtToken = authorizationHeader;
>>>>>>> 632baa1304b9f5bf2c200e5a9c5e9e0e40e04c94
            
            try {
                username = jwtUtil.extractUsername(jwtToken);
            } catch (Exception e) {
                // Invalid token - continue without authentication
                logger.warn("Failed to extract username from JWT: " + e.getMessage());
            }
        }

        // If we have a username and no authentication is set yet
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            try {
                // Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Validate token
                if (jwtUtil.validateToken(jwtToken, userDetails.getUsername())) {
                    
                    // Create authentication token
<<<<<<< HEAD
                    UsernamePasswordAuthenticationToken authenticationToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );
                    
                    authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
=======
                    UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );

                    // Set details to null to avoid Coder proxy interference
                    authenticationToken.setDetails(null);
>>>>>>> 632baa1304b9f5bf2c200e5a9c5e9e0e40e04c94

                    // Set authentication in Spring Security context
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    
                    logger.debug("JWT authentication successful for user: " + username);
                } else {
                    logger.warn("JWT token validation failed for user: " + username);
                }
            } catch (Exception e) {
                logger.error("Error during JWT authentication: " + e.getMessage());
            }
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
