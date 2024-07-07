package com.personal_projects.cloud_application.backend.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String createFolder(String folderName, String path);

    String saveFile(MultipartFile file, String path);

    String deleteFile(String path);

    public String renameFile(String path, String newName);
}
