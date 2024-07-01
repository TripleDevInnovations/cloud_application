package com.personal_projects.cloud_application.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.personal_projects.cloud_application.backend.dto.JwtAuthenticationResponse;
import com.personal_projects.cloud_application.backend.dto.SignInRequest;
import com.personal_projects.cloud_application.backend.dto.SignUpRequest;
import com.personal_projects.cloud_application.backend.dto.TokenRequest;
import com.personal_projects.cloud_application.backend.entities.Role;
import com.personal_projects.cloud_application.backend.entities.User;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import com.personal_projects.cloud_application.backend.services.JWTService;
import com.personal_projects.cloud_application.backend.services.impl.AuthenticationServiceImpl;

import java.util.HashMap;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    public void AuthenticationService_SignUp_ReturnsUserUser() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername(";pM_Vl##3QGNl6pX1IRY}#,_s'9UJ1");
        signUpRequest.setPassword(";pM_Vl##3QGNl6pX1IRY}#,_s'9UJ1");
        signUpRequest.setRole(Role.USER);

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Capture the User object when save is called and return it
        when(userRepo.save(any(User.class)))
                .thenAnswer(
                        invocation -> {
                            User savedUser = invocation.getArgument(0);
                            return savedUser; // Return the user that was passed to save, which should have the
                            // encoded password
                        });

        User savedUser = authenticationService.signUp(signUpRequest);

        Assertions.assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        Assertions.assertThat(savedUser.getUsername()).isEqualTo(";pM_Vl##3QGNl6pX1IRY}#,_s'9UJ1");
        Assertions.assertThat(savedUser.getRole()).isEqualTo(Role.USER);
    }

    @Test
    public void AuthenticationService_SignUp_ReturnsAdminUser() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername(";pM_Vl##3QGNl6pX1IRY}#,_s'9UJ1");
        signUpRequest.setPassword(";pM_Vl##3QGNl6pX1IRY}#,_s'9UJ1");
        signUpRequest.setRole(Role.ADMIN);

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Capture the User object when save is called and return it
        when(userRepo.save(any(User.class)))
                .thenAnswer(
                        invocation -> {
                            User savedUser = invocation.getArgument(0);
                            return savedUser; // Return the user that was passed to save, which should have the
                            // encoded password
                        });

        User savedUser = authenticationService.signUp(signUpRequest);

        Assertions.assertThat(savedUser).isNotNull();
        Assertions.assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        Assertions.assertThat(savedUser.getUsername()).isEqualTo(";pM_Vl##3QGNl6pX1IRY}#,_s'9UJ1");
        Assertions.assertThat(savedUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    public void AuthenticationService_SignUp_ReturnsOwnerUser() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername(";pM_Vl##3QGNl6pX1IRY}#,_s'9UJ1");
        signUpRequest.setPassword(";pM_Vl##3QGNl6pX1IRY}#,_s'9UJ1");
        signUpRequest.setRole(Role.OWNER);

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Capture the User object when save is called and return it
        when(userRepo.save(any(User.class)))
                .thenAnswer(
                        invocation -> {
                            User savedUser = invocation.getArgument(0);
                            return savedUser; // Return the user that was passed to save, which should have the
                            // encoded password
                        });

        User savedUser = authenticationService.signUp(signUpRequest);

        Assertions.assertThat(savedUser).isNotNull();
        Assertions.assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        Assertions.assertThat(savedUser.getUsername()).isEqualTo(";pM_Vl##3QGNl6pX1IRY}#,_s'9UJ1");
        Assertions.assertThat(savedUser.getRole()).isEqualTo(Role.OWNER);
    }

    @Test
    public void AuthenticationService_SignIn_ReturnsAuthenticationResponse() {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setUsername(";pM_Vl##3QGNl6pX1IRY}#,_s'9UJ1");
        signInRequest.setPassword(";pM_Vl##3QGNl6pX1IRY}#,_s'9UJ1");
        User user = new User();
        user.setUsername(signInRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signInRequest.getPassword()));

        Authentication auth = Mockito.mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        when(userRepo.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");
        when(jwtService.generateRefreshToken(any(HashMap.class), any(User.class)))
                .thenReturn("refreshToken");

        JwtAuthenticationResponse response = authenticationService.signin(signInRequest);

        Assertions.assertThat(response.getToken()).isEqualTo("jwtToken");
        Assertions.assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
    }

    @Test
    public void AuthenticationService_RefreshToken_ReturnsNewToken() {
        // Given
        TokenRequest refreshTokenRequest = new TokenRequest();
        refreshTokenRequest.setToken("validRefreshToken");

        User user = new User();
        user.setUsername("testUser");

        // Mock the JWTService to return the expected username and token validation result
        when(jwtService.extractUsername(refreshTokenRequest.getToken())).thenReturn(user.getUsername());
        when(jwtService.isTokenValid(refreshTokenRequest.getToken(), user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("newJwtToken");

        // Mock the UserRepo to return an Optional of User
        when(userRepo.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        // When
        JwtAuthenticationResponse jwtAuthenticationResponse =
                authenticationService.refreshToken(refreshTokenRequest);

        // Then
        Assertions.assertThat(jwtAuthenticationResponse).isNotNull();
        Assertions.assertThat(jwtAuthenticationResponse.getToken()).isEqualTo("newJwtToken");
        Assertions.assertThat(jwtAuthenticationResponse.getRefreshToken())
                .isEqualTo("validRefreshToken");
    }
}
