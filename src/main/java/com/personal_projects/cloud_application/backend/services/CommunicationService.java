package com.personal_projects.cloud_application.backend.services;

import java.util.Map;

public interface CommunicationService {
    Map<String, String> createErrorMessage(String error, String details);
}
