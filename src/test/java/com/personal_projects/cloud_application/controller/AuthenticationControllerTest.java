package com.personal_projects.cloud_application.controller;

import com.personal_projects.cloud_application.backend.controller.AuthenticationController;
import com.personal_projects.cloud_application.backend.dto.JwtAuthenticationResponse;
import com.personal_projects.cloud_application.backend.dto.SignInRequest;
import com.personal_projects.cloud_application.backend.dto.SignUpRequest;
import com.personal_projects.cloud_application.backend.dto.TokenRequest;
import com.personal_projects.cloud_application.backend.entities.Role;
import com.personal_projects.cloud_application.backend.entities.User;
import com.personal_projects.cloud_application.backend.services.*;
import com.personal_projects.cloud_application.CloudApplication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;

import org.hamcrest.CoreMatchers;

import java.util.HashMap;
import java.util.Map;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private CommunicationService communicationService;

    @MockBean
    private FileService fileService;

    @MockBean
    private CloudApplication cloudApplication;

    @Test
    public void authenticationController_Signup_ReturnsUser() throws Exception {
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("testuser");
        testUser.setRole(Role.USER);

        given(authenticationService.signUp(any(SignUpRequest.class))).willReturn(testUser);
        given(fileService.createFolder(anyString(), anyString())).willReturn(true);
        given(communicationService.createErrorMessage(anyString(), anyString())).willReturn(null);

        ResultActions response = mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignUpRequest())));

        response
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", CoreMatchers.is(testUser.getUsername())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.password", CoreMatchers.is(testUser.getPassword())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role", CoreMatchers.is("USER")));
    }

    @Test
    public void authenticationController_Signup_ReturnsAdminUser() throws Exception {
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("testuser");
        testUser.setRole(Role.ADMIN);
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("error", "Error, der für deen Test gemacht ist");
        errorMessage.put("details", "der wird benötigt, um die klasse zu testen");

        given(authenticationService.signUp(any(SignUpRequest.class))).willReturn(testUser);
        given(communicationService.createErrorMessage(anyString(), anyString())).willReturn(errorMessage);

        ResultActions response = mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignUpRequest())));

        response
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", CoreMatchers.is(testUser.getUsername())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.password", CoreMatchers.is(testUser.getPassword())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role", CoreMatchers.is("ADMIN")));
    }

    @Test
    public void authenticationController_Signup_ReturnsOwnerUser() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setRole(Role.OWNER);
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("testuser");
        testUser.setRole(Role.USER);
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("error", "Error, der für deen Test gemacht ist");
        errorMessage.put("details", "der wird benötigt, um die klasse zu testen");

        given(authenticationService.signUp(any(SignUpRequest.class))).willReturn(testUser);
        given(communicationService.createErrorMessage(anyString(), anyString())).willReturn(errorMessage);

        ResultActions response = mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        response
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error", CoreMatchers.is(errorMessage.get("error"))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details", CoreMatchers.is(errorMessage.get("details"))));
    }

    @Test
    public void authenticationController_SignIn_ReturnsTokens() throws Exception {
        JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
        jwtAuthenticationResponse.setToken("validToken");
        jwtAuthenticationResponse.setRefreshToken("refreshToken");

        given(authenticationService.signin(any(SignInRequest.class))).willReturn(jwtAuthenticationResponse);

        ResultActions response = mockMvc.perform(post("/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignInRequest())));

        response
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token", CoreMatchers.is(jwtAuthenticationResponse.getToken())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken", CoreMatchers.is(jwtAuthenticationResponse.getRefreshToken())));
    }

    @Test
    public void authenticationController_SignIn_ReturnsBadRequest() throws Exception {
        JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
        jwtAuthenticationResponse.setToken("validToken");
        jwtAuthenticationResponse.setRefreshToken("refreshToken");

        given(authenticationService.signin(any(SignInRequest.class))).willReturn(null);

        ResultActions response = mockMvc.perform(post("/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignInRequest())));

        response
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void authenticationController_Refresh_ReturnsResponse() throws Exception {
        JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
        jwtAuthenticationResponse.setToken("validToken");
        jwtAuthenticationResponse.setRefreshToken("refreshToken");

        given(authenticationService.refreshToken(any(TokenRequest.class))).willReturn(jwtAuthenticationResponse);

        ResultActions response = mockMvc.perform(post("/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TokenRequest())));

        response
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token", CoreMatchers.is(jwtAuthenticationResponse.getToken())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken", CoreMatchers.is(jwtAuthenticationResponse.getRefreshToken())));
    }

    @Test
    public void authenticationController_Refresh_ReturnsBadRequest() throws Exception {
        given(authenticationService.refreshToken(any(TokenRequest.class))).willReturn(null);

        ResultActions response = mockMvc.perform(post("/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TokenRequest())));

        response
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}
