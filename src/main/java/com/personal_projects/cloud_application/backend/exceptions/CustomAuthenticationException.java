package com.personal_projects.cloud_application.backend.exceptions;

public class CustomAuthenticationException extends Exception {
    public CustomAuthenticationException(String message) {
        super(message);
    }

    public CustomAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}