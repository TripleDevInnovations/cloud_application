package com.personal_projects.cloud_application.backend.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String createFolder(String folderName, String path);

    Boolean saveFile(MultipartFile file, String path);

    String deleteFile(String path);

    String renameFile(String path, String newName);

    String changeFilePath(String filePath, String newFileName);
}
