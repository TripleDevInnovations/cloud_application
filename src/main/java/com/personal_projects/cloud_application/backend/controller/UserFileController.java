package com.personal_projects.cloud_application.backend.controller;

import com.personal_projects.cloud_application.backend.repositories.UserFileRepo;
import com.personal_projects.cloud_application.backend.repositories.FolderRepo;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import com.personal_projects.cloud_application.backend.services.FileService;
import com.personal_projects.cloud_application.backend.services.JWTService;
import com.personal_projects.cloud_application.backend.entities.UserFile;
import com.personal_projects.cloud_application.backend.entities.Folder;
import com.personal_projects.cloud_application.backend.entities.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{username}")
public class UserFileController {

    private static final String FILE_EMPTY = "The file must not be empty.";
    private static final String ERROR_SAVING_FILE = "File could not be saved.";
    private static final String USER_NOT_ALLOWED = "User is not authorized.";
    private static final String MISSING_AUTHORIZATION = "The authorization is missing.";
    private static final String FILE_NOT_FOUND = "Requested file does not exist.";
    private static final String FOLDER_NOT_FOUND = "Requested Folder does not exist.";
    private static final String MISSING_ARGUMENTS = "Some information is missing.";

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserFileRepo userFileRepo;
    @Autowired
    private FolderRepo folderRepo;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private FileService fileService;

    private final static String BASE_PATH = "C:/Users/JAHEESE/Documents/cloudstorage/";

    @PostMapping("/file")
    public ResponseEntity<?> createFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int folderId, @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Error 111: " + FILE_EMPTY);
        }

        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error 112: " + MISSING_AUTHORIZATION);
        }

        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error 113: " + USER_NOT_ALLOWED);
        }

        User user = optionalUser.get();
        if (!jwtService.isTokenValid(token, user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error 114: " + USER_NOT_ALLOWED);

        }

        Optional<Folder> optionalFolder = folderRepo.findById(folderId);
        if (optionalFolder.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error 115: " + FOLDER_NOT_FOUND);
        }

        Folder folder = optionalFolder.get();
        if (folder.getUserId() != user.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error 116: " + USER_NOT_ALLOWED);
        }

        UserFile userFile = new UserFile();
        userFile.setFileName(file.getOriginalFilename());
        userFile.setFileType(file.getContentType());
        userFile.setSize(file.getSize());
        userFile.setFolderId(folderId);
        userFile.setUserId(user.getId());
        userFile = userFileRepo.save(userFile);

        if (fileService.saveFile(file, user.getId() + "/" + userFile.getId() + "." + fileService.getFileExtension(userFile.getFileName()))) {
            List<UserFile> files = folder.getFiles();
            files.add(userFile);
            folder.setFiles(files);
            folderRepo.save(folder);
            return ResponseEntity.ok(user);
        } else {
            userFileRepo.delete(userFile);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error 117: " + ERROR_SAVING_FILE);
        }
    }

    @GetMapping("/file")
    public ResponseEntity<?> readFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int fileId) {
        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error 121: " + MISSING_AUTHORIZATION);
        }

        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error 122: " + USER_NOT_ALLOWED);
        }

        User user = optionalUser.get();
        if (!jwtService.isTokenValid(token, user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error 123: " + USER_NOT_ALLOWED);
        }

        Optional<UserFile> optionalUserFile = userFileRepo.findById(fileId);
        if (optionalUserFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error 124: " + FILE_NOT_FOUND);
        }

        UserFile userFile = optionalUserFile.get();
        if (userFile.getUserId() != user.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error 125: " + USER_NOT_ALLOWED);
        }

        return ResponseEntity.ok(userFile);
    }

    @PutMapping("/file")
    public ResponseEntity<?> updateFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int fileId, @RequestParam("newname") String newFileName) {
        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error 131: " + MISSING_AUTHORIZATION);
        }
        if (newFileName.isEmpty()) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error 132: " + MISSING_ARGUMENTS);
        }

        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error 133: " + USER_NOT_ALLOWED);
        }

        User user = optionalUser.get();
        if (jwtService.isTokenValid(token, user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error 134: " + USER_NOT_ALLOWED);
        }

        Optional<UserFile> optionalUserFile = userFileRepo.findById(fileId);
        if (optionalUserFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error 135: " + FILE_NOT_FOUND);
        }

        int userId = user.getId();
        UserFile userFile = optionalUserFile.get();
        if (userFile.getUserId() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error 136: " + USER_NOT_ALLOWED);
        }

        String oldFileExtension = fileService.getFileExtension(userFile.getFileName());
        String newFileExtension = fileService.getFileExtension(newFileName);

        if (!Objects.equals(oldFileExtension, newFileExtension)) {
            String newInternName = userFile.getId() + "." + newFileExtension;
            fileService.renameFile(userId + "/" + userFile.getId() + "." + oldFileExtension, newInternName);

            try {
                Path path = new File(BASE_PATH + newInternName).toPath();
                userFile.setFileType(Files.probeContentType(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        userFile.setFileName(newFileName);
        userFileRepo.save(userFile);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/movefile")
    public ResponseEntity<?> moveFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("fileid") int fileId, @RequestParam("folderid") int newParentFolderId) {
        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error 141: " + MISSING_AUTHORIZATION);
        }
        if (fileId == 0 || newParentFolderId == 0) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error 142: " + MISSING_ARGUMENTS);
        }

        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error 143: " + USER_NOT_ALLOWED);
        }

        User user = optionalUser.get();
        if (!jwtService.isTokenValid(token, user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error 144: " + USER_NOT_ALLOWED);
        }

        Optional<UserFile> optionalUserFile = userFileRepo.findById(fileId);
        if (optionalUserFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error 145: " + FILE_NOT_FOUND);
        }

        Optional<Folder> optionalNewParentFolder = folderRepo.findById(newParentFolderId);
        if (optionalNewParentFolder.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error 146: " + FILE_NOT_FOUND);

        }

        int userId = user.getId();
        UserFile userFile = optionalUserFile.get();
        if (userFile.getUserId() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error 147: " + USER_NOT_ALLOWED);
        }

        Folder newParentFolder = optionalNewParentFolder.get();
        if (newParentFolder.getUserId() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error 148: " + USER_NOT_ALLOWED);
        }

        Optional<Folder> optionalOldParentFolder = folderRepo.findById(userFile.getFolderId());
        if (optionalOldParentFolder.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error 149: " + FOLDER_NOT_FOUND);
        }

        Folder oldParentFolder = optionalOldParentFolder.get();
        if (oldParentFolder.getUserId() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error 1410: " + USER_NOT_ALLOWED);
        }

        List<UserFile> oldUserFiles = oldParentFolder.getFiles();
        oldUserFiles.remove(userFile);
        oldParentFolder.setFiles(oldUserFiles);
        folderRepo.save(oldParentFolder);

        userFile.setFolderId(newParentFolderId);
        userFileRepo.save(userFile);

        List<UserFile> newUserFiles = newParentFolder.getFiles();
        newUserFiles.add(userFile);
        newParentFolder.setFiles(newUserFiles);
        folderRepo.save(newParentFolder);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("fileid") int fileId) {
        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(MISSING_AUTHORIZATION);
        }

        if (fileId == 0) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MISSING_ARGUMENTS);
        }

        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isEmpty() || !jwtService.isTokenValid(token, optionalUser.get())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(USER_NOT_ALLOWED);
        }

        int userId = optionalUser.get().getId();
        Optional<UserFile> optionalUserFile = userFileRepo.findById(fileId);
        if (optionalUserFile.isEmpty() || optionalUserFile.get().getUserId() != userId) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(FILE_NOT_FOUND);
        }

        UserFile userFile = optionalUserFile.get();
        Optional<Folder> optionalFolder = folderRepo.findById(userFile.getFolderId());
        if (optionalFolder.isEmpty() || optionalFolder.get().getUserId() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(USER_NOT_ALLOWED);
        }

        Folder folder = optionalFolder.get();
        List<UserFile> userFiles = folder.getFiles();
        boolean removed = userFiles.removeIf(userFile1 -> userFile1.equals(userFile));

        if (removed) {
            if (fileService.deleteFile(userId + "/" + userFile.getId() + "." + fileService.getFileExtension(userFile.getFileName()))) {
                folder.setFiles(userFiles);
                folderRepo.save(folder);
                userFileRepo.delete(userFile);
                return ResponseEntity.ok().build();                        }
            else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Löschen der Datei vom Dateisystem");
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Entfernen der Datei aus dem Ordner");
        }
    }

}
