package com.personal_projects.cloud_application.backend.services.impl;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.personal_projects.cloud_application.backend.services.AuthenticationService;
import com.personal_projects.cloud_application.backend.dto.JwtAuthenticationResponse;
import com.personal_projects.cloud_application.backend.repositories.FolderRepo;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import com.personal_projects.cloud_application.backend.services.JWTService;
import com.personal_projects.cloud_application.backend.dto.SignInRequest;
import com.personal_projects.cloud_application.backend.dto.SignUpRequest;
import com.personal_projects.cloud_application.backend.dto.TokenRequest;
import com.personal_projects.cloud_application.backend.entities.Folder;
import com.personal_projects.cloud_application.backend.entities.User;

import lombok.RequiredArgsConstructor;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private final UserRepo userRepo;
    private final FolderRepo folderRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @Override
    public User signUp(SignUpRequest signUpRequest) {
        if (userRepo.findByUsername(signUpRequest.getUsername()).isEmpty()) {
            User user = new User();
            user.setUsername(signUpRequest.getUsername());
            user.setRole(signUpRequest.getRole());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user = userRepo.save(user);

            // Erstelle den Root-Ordner für den Benutzer
            Folder rootFolder = new Folder();
            rootFolder.setFolderName(signUpRequest.getUsername());
            rootFolder.setUserId(user.getId());
            rootFolder.setParentFolderId(0);
            rootFolder = folderRepo.save(rootFolder);

            user.setRootFolder(rootFolder);

            // Speichere den Benutzer und den Ordner in der Datenbank
            user = userRepo.save(user);
            return user;
        }
        return null;
    }

    @Override
    public JwtAuthenticationResponse signin(SignInRequest signInRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    signInRequest.getUsername(), signInRequest.getPassword()));
        } catch (AuthenticationException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Authentication failed for user: {}", signInRequest.getUsername(), e);
            }
            return null;
        }
        var user = userRepo.findByUsername(signInRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        var jwt = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

        JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
        jwtAuthenticationResponse.setToken(jwt);
        jwtAuthenticationResponse.setRefreshToken(refreshToken);
        return jwtAuthenticationResponse;
    }

    @Override
    public JwtAuthenticationResponse refreshToken(TokenRequest refreshTokenRequest) {
        try {
            String username = jwtService.extractUsername(refreshTokenRequest.getToken());
            User user = userRepo.findByUsername(username).orElseThrow(
                    () -> new UsernameNotFoundException("User not found with username: " + username));

            if (jwtService.isTokenValid(refreshTokenRequest.getToken(), user)) {
                var jwt = jwtService.generateToken(user);
                JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
                jwtAuthenticationResponse.setToken(jwt);
                jwtAuthenticationResponse.setRefreshToken(refreshTokenRequest.getToken());
                return jwtAuthenticationResponse;
            } else {
                return null;
            }
        } catch (AuthenticationException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Token refresh failed", e);
            }
            return null;
        }
    }
}
