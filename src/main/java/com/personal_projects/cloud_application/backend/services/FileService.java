package com.personal_projects.cloud_application.backend.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    boolean createFolder(String folderName, String path);

    boolean saveFile(MultipartFile file, String path);

    boolean deleteFile(String path);

    boolean renameFile(String path, String newName);

    boolean moveFile(String oldPath, String newPath);

    String changeFilePath(String filePath, String newFileName);

    String getFileExtension(String input);
}
