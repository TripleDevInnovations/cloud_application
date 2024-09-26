package com.personal_projects.cloud_application.backend.services.impl;

import com.personal_projects.cloud_application.backend.services.FileService;

import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;

import java.io.IOException;
import java.io.File;

import org.slf4j.Logger;

@Service
public class FileServiceImpl implements FileService {

    private final static String BASEPATH = "C:/Users/JAHEESE/Documents/cloudstorage/";

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public boolean createFolder(String folderName, String path) {
        File folder = new File(BASEPATH + path + File.separator + folderName);
        return folder.mkdirs();
    }

    @Override
    public boolean saveFile(MultipartFile file, String path) {
        if (file.isEmpty()) {
            return false;
        }

        try {
            // Erstelle den Pfad, unter dem die Datei gespeichert wird
            Path filePath = Paths.get(BASEPATH + path);
            Files.write(filePath, file.getBytes());

            return true;
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.toString());
            }
            return false;
        }
    }

    @Override
    public boolean deleteFile(String path) {
        Path filePath = Paths.get(BASEPATH + path);
        File file = filePath.toFile();
        return file.exists() && file.delete();
    }

    @Override
    public boolean renameFile(String path, String newName) {
        File file = new File(BASEPATH + path);

        if (!file.exists()) {
            return false;
        }

        String parentPath = file.getParent();
        File newFile = new File(parentPath + File.separator + newName);

        return file.renameTo(newFile);
    }

    @Override
    public boolean moveFile(String oldPath, String newPath) {
        Path sourcePath = Paths.get(BASEPATH + oldPath);
        Path destinationPath = Paths.get(BASEPATH + newPath);

        try {
            Files.move(sourcePath, destinationPath);
            return true;
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.toString());
            }
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
        int lastSeparatorIndex = filePath.lastIndexOf('/');

        // Falls der Dateitrenner nicht gefunden wird, wird eine Ausnahme geworfen
        if (lastSeparatorIndex == -1) {
            throw new IllegalArgumentException("Invalid file path format");
        }

        // Den Verzeichnispfad extrahieren
        String directoryPath = filePath.substring(0, lastSeparatorIndex + 1);

        // Den neuen Dateipfad zusammensetzen

        return directoryPath + newFileName;
    }

    @Override
    public String getFileExtension(String input) {
        if (input == null || !input.contains(".")) {
            return null;
        }
        return input.substring(input.lastIndexOf('.') + 1);
    }


}
