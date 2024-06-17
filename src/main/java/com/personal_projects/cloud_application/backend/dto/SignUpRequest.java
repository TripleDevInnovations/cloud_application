package com.personal_projects.cloud_application.backend.dto;

import com.personal_projects.cloud_application.backend.entities.Role;
import lombok.Data;

@Data
public class SignUpRequest {
  private String username;
  private String password;
  private Role role;
}
