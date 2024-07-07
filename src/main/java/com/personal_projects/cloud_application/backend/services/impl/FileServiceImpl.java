package com.personal_projects.cloud_application.backend.services.impl;

import com.personal_projects.cloud_application.backend.services.FileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {

    private final static String basePath = "C:/Users/Jacko/Documents/cloudstorage/";

    @Override
    public String createFolder(String folderName, String path) {
        File folder = new File(basePath + path + File.separator + folderName);
        if (folder.mkdirs()) {
            return "Folder created successfully.";
        } else {
            return "Failed to create folder.";
        }
    }

    @Override
    public String saveFile(MultipartFile file, String path) {
        if (file.isEmpty()) {
            return "Bitte wähle eine Datei aus!";
        }

        try {
            // Stelle sicher, dass das Verzeichnis existiert
            File directory = new File(basePath + path);

            // Erstelle den Pfad, unter dem die Datei gespeichert wird
            Path filePath = Paths.get(basePath + path + "/" + file.getOriginalFilename());
            Files.write(filePath, file.getBytes());

            return "Datei erfolgreich hochgeladen";
        } catch (IOException e) {
            return "Fehler beim Hochladen der Datei: " + e.getMessage();
        }
    }

    @Override
    public String deleteFile(String path) {
        Path filePath = Paths.get(path);
        File file = filePath.toFile();
        if (file.exists()) {
            if (file.delete()) {
                return "Datei erfolgreich gelöscht";
            } else {
                return "Fehler beim Löschen der Datei";
            }
        } else {
            return "Datei nicht gefunden";
        }
    }

    @Override
    public String renameFile(String path, String newName) {
        String fullPath = basePath + path;
        System.out.println(fullPath);
        File file = new File(basePath + path);

        if (!file.exists()) {
            return "File or directory does not exist.";
        }

        String parentPath = file.getParent();
        File newFile = new File(parentPath + File.separator + newName);

        if (file.renameTo(newFile)) {
            return "File or directory renamed successfully.";
        } else {
            return "Failed to rename file or directory.";
        }
    }
}
