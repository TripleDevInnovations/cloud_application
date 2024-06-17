package com.personal_projects.cloud_application.backend.services.impl;

import com.personal_projects.cloud_application.backend.services.DirectoryFileService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DirectoryFileServiceImpl implements DirectoryFileService {

  public String saveFile(MultipartFile file, String path) {
    if (file.isEmpty()) {
      return "Bitte wähle eine Datei aus!";
    }

    try {
      // Stelle sicher, dass das Verzeichnis existiert
      File directory = new File(path);
      if (!directory.exists()) {
        directory.mkdirs();
      }

      // Erstelle den Pfad, unter dem die Datei gespeichert wird
      Path filePath = Paths.get(path + file.getOriginalFilename());
      Files.write(filePath, file.getBytes());

      return "Datei erfolgreich hochgeladen";
    } catch (IOException e) {
      return "Fehler beim Hochladen der Datei: " + e.getMessage();
    }
  }

  public String deleteFile(String path) {
    try {
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
    } catch (Exception e) {
      return "Fehler beim Löschen der Datei: " + e.getMessage();
    }
  }
}
