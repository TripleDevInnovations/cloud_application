package com.personal_projects.cloud_application.backend.services;

import com.personal_projects.cloud_application.backend.dto.JwtAuthenticationResponse;
import com.personal_projects.cloud_application.backend.dto.SignInRequest;
import com.personal_projects.cloud_application.backend.dto.SignUpRequest;
import com.personal_projects.cloud_application.backend.dto.TokenRequest;
import com.personal_projects.cloud_application.backend.entities.User;

public interface AuthenticationService {

  User signUp(SignUpRequest signUpRequest);

  JwtAuthenticationResponse signin(SignInRequest signinRequest);

  JwtAuthenticationResponse refreshToken(TokenRequest refreshTokenRequest);
}
