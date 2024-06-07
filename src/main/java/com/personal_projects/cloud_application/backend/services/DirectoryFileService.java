package com.personal_projects.cloud_application.backend.services;

import org.springframework.web.multipart.MultipartFile;


public interface DirectoryFileService {

    String saveFile(MultipartFile file, String path);
    String deleteFile(String path);
}
