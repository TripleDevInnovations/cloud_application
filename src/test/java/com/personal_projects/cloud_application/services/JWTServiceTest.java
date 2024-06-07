package com.personal_projects.cloud_application.services;

import com.personal_projects.cloud_application.backend.services.impl.JWTServiceImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class JWTServiceTest {

    @InjectMocks
    private JWTServiceImpl jwtService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    public void setUp() {
        jwtService = new JWTServiceImpl();
        userDetails = new User("username", "password", Collections.emptyList());
    }

    private Key getTestSignKey() {
        // Verwende denselben Schlüssel, der in der getSignKey() Methode definiert ist
        byte[] keyBytes = Decoders.BASE64.decode("413F4428472B4B6250655368566D5970337336763979244226452948404D6351");
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    public void generateToken_ShouldReturnTokenForUserDetails() {

        String token = jwtService.generateToken(userDetails);
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    public void generateRefreshToken_ShouldReturnRefreshTokenForUserDetails() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("extra", "claim");

        String refreshToken = jwtService.generateRefreshToken(claims, userDetails);
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
    }

    @Test
    public void extractUsername_ShouldReturnUsernameFromToken() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("username");
    }

    @Test
    public void isTokenValid_ShouldReturnTrueForValidToken() {
        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertThat(isValid).isTrue();
    }

    @Test
    public void isTokenValid_ShouldReturnFalseForExpiredToken() {
        // Erstelle ein abgelaufenes Token mit dem Test-Schlüssel
        UserDetails expiredUserDetails = new User("username", "password", new ArrayList<>());
        String expiredToken = Jwts.builder()
                .setSubject(expiredUserDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)) // Ausgestellt vor 24 Stunden
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 23)) // Abgelaufen vor 23 Stunden
                .signWith(getTestSignKey(), SignatureAlgorithm.HS256)
                .compact();

        JWTServiceImpl jwtService = new JWTServiceImpl();
        // test die Methode isTokenValid
        try {
            jwtService.isTokenValid(expiredToken, expiredUserDetails);
        } catch (ExpiredJwtException e) {
            String errorMessage = e.getMessage();
            System.out.println("error message: " + errorMessage);
            assertThat(errorMessage).startsWith("JWT expired at ");
        }
    }
}
