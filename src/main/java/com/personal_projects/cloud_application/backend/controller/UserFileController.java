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
            return ResponseEntity.badRequest().body(FILE_EMPTY);
        } else if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(MISSING_AUTHORIZATION);
        }

        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isEmpty() || !jwtService.isTokenValid(token, optionalUser.get())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(USER_NOT_ALLOWED);
        }

        User user = optionalUser.get();
        Optional<Folder> optionalFolder = folderRepo.findById(folderId);
        if (optionalFolder.isEmpty() || optionalFolder.get().getUserId() != user.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(USER_NOT_ALLOWED);
        }

        UserFile userFile = new UserFile();
        userFile.setFileName(file.getOriginalFilename());
        userFile.setFileType(file.getContentType());
        userFile.setSize(file.getSize());
        userFile.setFolderId(folderId);
        userFile.setUserId(user.getId());
        userFile = userFileRepo.save(userFile);

        Folder folder = optionalFolder.get();
        if (fileService.saveFile(file, user.getId() + "/" + userFile.getId() + "." + fileService.getFileExtension(userFile.getFileName()))) {
            List<UserFile> files = folder.getFiles();
            files.add(userFile);
            folder.setFiles(files);
            folderRepo.save(folder);
            return ResponseEntity.ok(user);
        } else {
            userFileRepo.delete(userFile);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_SAVING_FILE);
        }
    }

    @GetMapping("/file")
    public ResponseEntity<?> readFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int fileId) {
        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(MISSING_AUTHORIZATION);
        }

        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isEmpty() || !jwtService.isTokenValid(token, optionalUser.get())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(USER_NOT_ALLOWED);
        }

        Optional<UserFile> optionalUserFile = userFileRepo.findById(fileId);
        if (optionalUserFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(FILE_NOT_FOUND);
        }

        if (optionalUserFile.get().getUserId() != optionalUser.get().getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(USER_NOT_ALLOWED);
        }

        return ResponseEntity.ok(optionalUserFile.get());
    }

    @PutMapping("/file")
    public ResponseEntity<?> updateFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int fileId, @RequestParam("newname") String newFileName) {
        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(MISSING_AUTHORIZATION);
        }
        if (newFileName.isEmpty()) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MISSING_ARGUMENTS);
        }

        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isEmpty() || !jwtService.isTokenValid(token, optionalUser.get())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(USER_NOT_ALLOWED);
        }

        Optional<UserFile> optionalUserFile = userFileRepo.findById(fileId);
        if (optionalUserFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(FILE_NOT_FOUND);
        }

        int userId = optionalUser.get().getId();
        UserFile userFile = optionalUserFile.get();
        if (userFile.getUserId() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(USER_NOT_ALLOWED);
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
    public ResponseEntity<?> moveFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("fileid") int fileId, @RequestParam("folderid") int folderId) {
        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(MISSING_AUTHORIZATION);
        }
        if (fileId == 0 || folderId == 0) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MISSING_ARGUMENTS);
        }

        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
            Optional<UserFile> optionalUserFile = userFileRepo.findById(fileId);
            Optional<Folder> optionalFolder = folderRepo.findById(folderId);
            if (optionalUserFile.isPresent() && optionalFolder.isPresent()) {
                UserFile userFile = optionalUserFile.get();
                Folder newFolder = optionalFolder.get();
                if (userFile.getUser().equals(username) && newFolder.getUser().equals(username)) {
                    Optional<Folder> optionalOldFolder = folderRepo.findById(userFile.getFolderId());
                    if (optionalOldFolder.isPresent() && optionalOldFolder.get().getUser().equals(username)) {
                        Folder oldFolder = optionalOldFolder.get();
                        List<UserFile> oldUserFiles = oldFolder.getFiles();
                        oldUserFiles.remove(userFile);
                        oldFolder.setFiles(oldUserFiles);
                        folderRepo.save(oldFolder);

                        List<UserFile> newUserFiles = newFolder.getFiles();
                        newUserFiles.add(userFile);
                        newFolder.setFiles(newUserFiles);
                        folderRepo.save(newFolder);

                        String oldPath = userFile.getPath();
                        userFile.setFolderId(newFolder.getId());
                        userFile.setPath(newFolder.getPath() + "/" + userFile.getFileName());
                        userFileRepo.save(userFile);

                        if (fileService.moveFile(oldPath, userFile.getPath())) {
                            return ResponseEntity.ok().build();
                        } else {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Verschieben der Datei");
                        }
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Alter Ordner nicht gefunden oder Benutzer nicht berechtigt");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Benutzer nicht berechtigt");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Datei oder neuer Ordner nicht gefunden");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Benutzer nicht gefunden oder Token ungültig");
        }
    }
//
//    @DeleteMapping("/file")
//    public ResponseEntity<?> deleteFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("fileid") int fileId) {
//        Optional<User> optionalUser = userRepo.findByUsername(username);
//        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
//            Optional<UserFile> optionalUserFile = userFileRepo.findById(fileId);
//            if (optionalUserFile.isPresent() && optionalUserFile.get().getUser().equals(username)) {
//                UserFile userFile = optionalUserFile.get();
//                Optional<Folder> optionalFolder = folderRepo.findById(userFile.getFolderId());
//                if (optionalFolder.isPresent() && optionalFolder.get().getUser().equals(username)) {
//                    Folder folder = optionalFolder.get();
//                    List<UserFile> userFiles = folder.getFiles();
//                    boolean removed = userFiles.removeIf(userFile1 -> userFile1.equals(userFile));
//
//                    if (removed) {
//                        if (fileService.deleteFile(userFile.getPath())) {
//                            folder.setFiles(userFiles);
//                            folderRepo.save(folder);
//                            userFileRepo.delete(userFile);
//                            return ResponseEntity.ok().build();                        }
//                        else {
//                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Löschen der Datei vom Dateisystem");
//                        }
//                    } else {
//                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Entfernen der Datei aus dem Ordner");
//                    }
//                } else {
//                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ordner nicht gefunden oder Benutzer nicht berechtigt");
//                }
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Datei nicht gefunden oder Benutzer nicht berechtigt");
//            }
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Benutzer nicht gefunden oder Token ungültig");
//        }
//    }

}
