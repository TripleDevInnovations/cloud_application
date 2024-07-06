package com.personal_projects.cloud_application.backend.controller;

import com.personal_projects.cloud_application.backend.repositories.UserFileRepo;
import com.personal_projects.cloud_application.backend.repositories.FolderRepo;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import com.personal_projects.cloud_application.backend.services.FolderService;
import com.personal_projects.cloud_application.backend.services.JWTService;
import com.personal_projects.cloud_application.backend.entities.Folder;
import com.personal_projects.cloud_application.backend.entities.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.apache.commons.lang3.StringUtils;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{username}")
public class FolderController {

    private static final String FOLDER_PATH = "/folder";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ERROR_SAVING_FOLDER = "Fehler beim Speichern des Ordners.";
    private static final String ERROR_DELETING_FOLDER = "";
    private static final String USER_NOT_FOUND_OR_UNAUTHORIZED = "Benutzer nicht gefunden oder Token ungültig.";
    private static final String FOLDER_NOT_FOUND_OR_UNAUTHORIZED = "Ordner nicht gefunden oder Benutzer nicht berechtigt.";
    private static final String PARENT_FOLDER_NOT_FOUND_OR_UNAUTHORIZED = "Übergeordneter Ordner nicht gefunden oder Benutzer nicht berechtigt.";
    private static final String FOLDER_NAME_EMPTY = "Ordnername darf nicht leer sein.";
    private static final String NEW_FOLDER_NAME_EMPTY = "Neuer Ordnername darf nicht leer sein.";
    private static final String FOLDER_CANNOT_BE_RENAMED = "Dieser Ordner darf nicht umbenannt werden.";
    private static final String FOLDER_CANNOT_BE_MOVED = "Dieser Ordner darf nicht verschoben werden.";
    private static final String FOLDER_CANNOT_BE_DELETED = "Dieser Ordner darf nicht gelöscht werden.";

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private FolderRepo folderRepo;
    @Autowired
    private UserFileRepo userFileRepo;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private FolderService folderService;

    @PostMapping(FOLDER_PATH)
    public ResponseEntity<?> createFolder(@PathVariable String username, @RequestHeader(AUTHORIZATION_HEADER) String token, @RequestParam("id") int parentFolderId, @RequestParam("foldername") String folderName) {
        if (StringUtils.isBlank(folderName)) {
            return ResponseEntity.badRequest().body(FOLDER_NAME_EMPTY);
        }

        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
            Optional<Folder> optionalFolder = folderRepo.findById(parentFolderId);
            int userId = optionalUser.get().getId();
            if (optionalFolder.isPresent() && isUserAuthorized(optionalFolder.get(), userId)) {
                Folder folder = optionalFolder.get();
                List<Folder> folders = folder.getFolders();

                Folder newFolder = new Folder();
                newFolder.setFolderName(folderName);
                newFolder.setUserId(userId);
                newFolder.setParentFolderId(folder.getId());

                try {
                    newFolder = folderRepo.save(newFolder);
                    folders.add(newFolder);
                    folder.setFolders(folders);
                    folderRepo.save(folder);
                    return ResponseEntity.ok().build();
                } catch (DataAccessException dae) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_SAVING_FOLDER);
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(PARENT_FOLDER_NOT_FOUND_OR_UNAUTHORIZED);
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(USER_NOT_FOUND_OR_UNAUTHORIZED);
        }
    }

    @GetMapping(FOLDER_PATH)
    public ResponseEntity<?> readFolder(@PathVariable String username, @RequestHeader(AUTHORIZATION_HEADER) String token, @RequestParam("id") int folderId) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
            Optional<Folder> optionalFolder = folderRepo.findById(folderId);
            int userId = optionalUser.get().getId();
            if (optionalFolder.isPresent() && isUserAuthorized(optionalFolder.get(), userId)) {
                return ResponseEntity.ok(optionalFolder.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(FOLDER_NOT_FOUND_OR_UNAUTHORIZED);
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(USER_NOT_FOUND_OR_UNAUTHORIZED);
        }
    }

    @PutMapping(FOLDER_PATH)
    public ResponseEntity<?> updateFolder(@PathVariable String username, @RequestHeader(AUTHORIZATION_HEADER) String token, @RequestParam("id") int folderId, @RequestParam("newname") String newFolderName) {
        if (StringUtils.isBlank(newFolderName)) {
            return ResponseEntity.badRequest().body(NEW_FOLDER_NAME_EMPTY);
        }
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isEmpty() || !jwtService.isTokenValid(token, optionalUser.get())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(USER_NOT_FOUND_OR_UNAUTHORIZED);
        }

        Optional<Folder> optionalFolder = folderRepo.findById(folderId);
        if (optionalFolder.isEmpty() || !isUserAuthorized(optionalFolder.get(), optionalUser.get().getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(PARENT_FOLDER_NOT_FOUND_OR_UNAUTHORIZED);
        }

        Folder folder = optionalFolder.get();
        if (folder.getParentFolderId() == 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FOLDER_CANNOT_BE_RENAMED);
        }

        return folderService.updateFolderName(folder, newFolderName);
    }

    @PutMapping("/movefolder")
    public ResponseEntity<?> moveFolder(@PathVariable String username, @RequestHeader(AUTHORIZATION_HEADER) String token, @RequestParam("id") int id, @RequestParam("targetid") int newParentId) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isEmpty() || !jwtService.isTokenValid(token, optionalUser.get())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(USER_NOT_FOUND_OR_UNAUTHORIZED);
        }

        int userId = optionalUser.get().getId();
        Optional<Folder> optionalFolder = folderRepo.findById(id);
        Optional<Folder> optionalNewFolder = folderRepo.findById(newParentId);

        if (optionalFolder.isEmpty() || optionalNewFolder.isEmpty() || !isUserAuthorized(optionalFolder.get(), userId) || !isUserAuthorized(optionalNewFolder.get(), userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ordner oder neuer Elternordner nicht gefunden oder Benutzer nicht berechtigt.");
        }

        Folder folder = optionalFolder.get();
        if (folder.getParentFolderId() == 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FOLDER_CANNOT_BE_MOVED);
        }

        return folderService.moveFolderToNewParent(folder, optionalNewFolder.get(), userId);
    }

    @DeleteMapping(FOLDER_PATH)
    public ResponseEntity<?> deleteFolder(@PathVariable String username, @RequestHeader(AUTHORIZATION_HEADER) String token, @RequestParam("id") int id) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isEmpty() || !jwtService.isTokenValid(token, optionalUser.get())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(USER_NOT_FOUND_OR_UNAUTHORIZED);
        }

        Optional<Folder> optionalFolder = folderRepo.findById(id);
        int userId = optionalUser.get().getId();
        if (optionalFolder.isEmpty() || !isUserAuthorized(optionalFolder.get(), userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(FOLDER_NOT_FOUND_OR_UNAUTHORIZED);
        }

        Folder folder = optionalFolder.get();
        if (folder.getParentFolderId() == 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(FOLDER_CANNOT_BE_DELETED);
        }

        Optional<Folder> optionalParentFolder = folderRepo.findById(folder.getParentFolderId());
        if (optionalParentFolder.isEmpty() || !isUserAuthorized(optionalParentFolder.get(), userId)) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(FOLDER_CANNOT_BE_DELETED);
        }

        return folderService.removeFolder(folder, optionalParentFolder.get());
    }

    private boolean isUserAuthorized(Folder folder, int userId) {
        return folder.getUserId() == userId;
    }
}