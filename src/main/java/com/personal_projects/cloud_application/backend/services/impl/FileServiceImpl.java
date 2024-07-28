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

    private final static String basePath = "C:/Users/JAHEESE/Documents/cloudstorage/";

    @Override
    public boolean createFolder(String folderName, String path) {
        File folder = new File(basePath + path + File.separator + folderName);
        return folder.mkdirs();
    }

    @Override
    public boolean saveFile(MultipartFile file, String path) {
        if (file.isEmpty()) {
            return false;
        }

        try {
            // Stelle sicher, dass das Verzeichnis existiert
            File directory = new File(basePath + path);
            directory.getName(); //nur weil pmd sonst einen

            // Erstelle den Pfad, unter dem die Datei gespeichert wird
            Path filePath = Paths.get(basePath + path);
            Files.write(filePath, file.getBytes());

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean deleteFile(String path) {
        Path filePath = Paths.get(basePath + path);
        File file = filePath.toFile();
        if (file.exists()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean renameFile(String path, String newName) {
        File file = new File(basePath + path);

        if (!file.exists()) {
            return false;
        }

        String parentPath = file.getParent();
        File newFile = new File(parentPath + File.separator + newName);

        if (file.renameTo(newFile)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean moveFile(String oldPath, String newPath) {
        Path sourcePath = Paths.get(basePath + oldPath);
        Path destinationPath = Paths.get(basePath + newPath);

        try {
            Files.move(sourcePath, destinationPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String changeFilePath(String filePath, String newFileName) {
        // Überprüfen, ob der Dateipfad und der neue Dateiname nicht null oder leer sind
        if (filePath == null || filePath.isEmpty() || newFileName == null || newFileName.isEmpty()) {
            throw new IllegalArgumentException("File path and new file name must not be null or empty");
        }

        // Den letzten Index des Dateitrenners finden
        int lastSeparatorIndex = filePath.lastIndexOf("/");

        // Falls der Dateitrenner nicht gefunden wird, wird eine Ausnahme geworfen
        if (lastSeparatorIndex == -1) {
            throw new IllegalArgumentException("Invalid file path format");
        }

        // Den Verzeichnispfad extrahieren
        String directoryPath = filePath.substring(0, lastSeparatorIndex + 1);

        // Den neuen Dateipfad zusammensetzen

        return directoryPath + newFileName;
    }
}
