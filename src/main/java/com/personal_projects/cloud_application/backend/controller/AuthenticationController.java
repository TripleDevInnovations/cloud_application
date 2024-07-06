package com.personal_projects.cloud_application.backend.controller;

import com.personal_projects.cloud_application.backend.services.AuthenticationService;
import com.personal_projects.cloud_application.backend.dto.JwtAuthenticationResponse;
import com.personal_projects.cloud_application.backend.services.CommunicationService;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import com.personal_projects.cloud_application.backend.services.FileService;
import com.personal_projects.cloud_application.backend.services.JWTService;
import com.personal_projects.cloud_application.backend.dto.SignInRequest;
import com.personal_projects.cloud_application.backend.dto.SignUpRequest;
import com.personal_projects.cloud_application.backend.dto.TokenRequest;
import com.personal_projects.cloud_application.backend.entities.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
//
//    @Autowired
//    private UserRepo userRepo;
//
//    @Autowired
//    private JWTService jwtService;

    @Autowired
    private CommunicationService communicationService;

    @Autowired
    private FileService fileService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest signUpRequest) {
        if (signUpRequest.isOwnerRole()) {
            return ResponseEntity.badRequest().body(communicationService.createErrorMessage(
                    "Die Registrierung ist fehlgeschlagen.", "Du darfst keinen Nutzer mit der Rolle: OWNER erstellen"));
        }
        User user = authenticationService.signUp(signUpRequest);
        if (user != null) {
            fileService.createFolder(String.valueOf(user.getId()), "");
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.badRequest().body(communicationService.createErrorMessage(
                    "Die Registrierung ist fehlgeschlagen.", "Benutzername ist bereits vergeben."));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponse> signin(@RequestBody SignInRequest signInRequest) {
        JwtAuthenticationResponse response = authenticationService.signin(signInRequest);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refresh(@RequestBody TokenRequest token) {
        JwtAuthenticationResponse response = authenticationService.refreshToken(token);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }
}
