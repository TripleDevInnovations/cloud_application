package com.personal_projects.cloud_application.backend.services.impl;

import com.personal_projects.cloud_application.backend.services.CommunicationService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CommunicationServiceImpl implements CommunicationService {

  public Map<String, String> createErrorMessage(String error, String details) {
    Map<String, String> errorMessage = new HashMap<>();
    errorMessage.put("error", error);
    errorMessage.put("details", details);
    return errorMessage;
  }
}
