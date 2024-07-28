package com.personal_projects.cloud_application.backend.controller;

import com.personal_projects.cloud_application.backend.entities.Folder;
import com.personal_projects.cloud_application.backend.entities.User;
import com.personal_projects.cloud_application.backend.repositories.FolderRepo;
import com.personal_projects.cloud_application.backend.repositories.UserFileRepo;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import com.personal_projects.cloud_application.backend.services.FileService;
import com.personal_projects.cloud_application.backend.services.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{username}")
public class FolderController {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private FolderRepo folderRepo;
    @Autowired
    private UserFileRepo userFileRepo;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private FileService fileService;

    @PostMapping("/folder")
    public ResponseEntity<?> createFolder(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int folderId, @RequestParam("foldername") String folderName) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
            Optional<Folder> optionalFolder = folderRepo.findById(folderId);
            int userId = optionalUser.get().getId();
            if (optionalFolder.isPresent() && optionalFolder.get().getUserId() == userId) {
                Folder folder = optionalFolder.get();
                List<Folder> folders = folder.getFolders();

                Folder newFolder = new Folder();
                newFolder.setFolderName(folderName);
                newFolder.setUserId(userId);
                newFolder.setParentFolderId(folder.getId());
                newFolder.setPath(folder.getPath() + "/" + folderName);
                newFolder = folderRepo.save(newFolder);
                folders.add(newFolder);
                folder.setFolders(folders);
                folderRepo.save(folder);
                if (fileService.createFolder(folderName, folder.getPath())) {
                    return ResponseEntity.ok().build();
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Erstellen des Ordners im Dateisystem");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Übergeordneter Ordner nicht gefunden oder Benutzer nicht berechtigt");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Benutzer nicht gefunden oder Token ungültig");
        }
    }

    @GetMapping("/folder")
    public ResponseEntity<?> readFolder(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int folderId) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
            Optional<Folder> optionalFolder = folderRepo.findById(folderId);
            int userId = optionalUser.get().getId();
            if (optionalFolder.isPresent() && optionalFolder.get().getUserId() == userId) {
                return ResponseEntity.ok(optionalFolder.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ordner nicht gefunden oder Benutzer nicht berechtigt");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Benutzer nicht gefunden oder Token ungültig");
        }
    }

    @PutMapping("/file")
    public ResponseEntity<?> updateFolder(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int folderId, @RequestParam("newname") String newFolderName) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
            Optional<Folder> optionalFolder = folderRepo.findById(folderId);
            int userId = optionalUser.get().getId();
            if (optionalFolder.isPresent() && optionalFolder.get().getUserId() == userId) {
                Folder folder = optionalFolder.get();
                if (fileService.renameFile(folder.getPath(), newFolderName)) {
                    folder.setFolderName(newFolderName);
                    folder.setPath(fileService.changeFilePath(folder.getPath(), newFolderName));
                    folderRepo.save(folder);
                    return ResponseEntity.ok(optionalFolder.get());
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ordner nicht gefunden, der umbenannt werden soll");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Übergeordneter Ordner nicht gefunden oder Benutzer nicht berechtigt");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Benutzer nicht gefunden oder Token ungültig");
        }
    }

    @PutMapping("")
    public ResponseEntity<?> moveFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("oldid") int oldId, @RequestParam("newid") int newId) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
            Optional<Folder> optionalOldFolder = folderRepo.findById(oldId);
            Optional<Folder> optionalNewFolder = folderRepo.findById(newId);
            int userId = optionalUser.get().getId();
            if (optionalOldFolder.isPresent() && optionalNewFolder.isPresent() && optionalOldFolder.get().getUserId() == userId && optionalNewFolder.get().getUserId() == userId) {

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("parent-folder oder Zielordner nicht gefunden oder Benutzer nicht berechtigt");
            }
        }
    }
}
