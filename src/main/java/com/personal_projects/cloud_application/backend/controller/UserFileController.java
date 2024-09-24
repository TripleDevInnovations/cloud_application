package com.personal_projects.cloud_application.backend.controller;

import com.personal_projects.cloud_application.backend.repositories.UserFileRepo;
import com.personal_projects.cloud_application.backend.repositories.FolderRepo;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import com.personal_projects.cloud_application.backend.services.FileService;
import com.personal_projects.cloud_application.backend.services.JWTService;
import com.personal_projects.cloud_application.backend.entities.UserFile;
import com.personal_projects.cloud_application.backend.entities.Folder;
import com.personal_projects.cloud_application.backend.entities.User;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{username}")
public class UserFileController {

    private static final String FILE_EMPTY = "The file must not be empty.";
    private static final String ERROR_SAVING_FILE = "File could not be saved.";
    private static final String USER_NOT_ALLOWED = "User is not authorized.";

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

    private final static String basePath = "C:/Users/JAHEESE/Documents/cloudstorage/";

    @PostMapping("/file")
    public ResponseEntity<?> createFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int folderId, @RequestParam("file") MultipartFile file) {
        if (file == null) {
            return ResponseEntity.badRequest().body(FILE_EMPTY);
        }

        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isEmpty() || !jwtService.isTokenValid(token, optionalUser.get())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(USER_NOT_ALLOWED);
        }

        User user = optionalUser.get();
        Optional<Folder> optionalFolder = folderRepo.findById(folderId);
        if (optionalUser.isEmpty() || optionalFolder.get().getUserId() != user.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(USER_NOT_ALLOWED);
        }
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get()) && !file.isEmpty()) {
            if(optionalFolder.isPresent() && optionalFolder.get().getUserId() == user.getId()) {
                Folder folder = optionalFolder.get();

                UserFile userFile = new UserFile();
                userFile.setFileName(file.getOriginalFilename());
                userFile.setFileType(file.getContentType());
                userFile.setSize(file.getSize());
                userFile.setFolderId(folderId);
                userFile.setUserId(user.getId());
                userFile = userFileRepo.save(userFile);

                if (fileService.saveFile(file, user.getId() + "/" + folder.getId() + "." + fileService.getFileExtension(userFile.getFileName()))) {
                    List<UserFile> files = folder.getFiles();
                    files.add(userFile);
                    folder.setFiles(files);
                    return ResponseEntity.ok(user);
                } else {
                    userFileRepo.delete(userFile);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_SAVING_FILE);
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this folder");
            }
        }

    }

//    @GetMapping("/file")
//    public ResponseEntity<UserFile> readFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int fileId) {
//        Optional<User> optionalUser = userRepo.findByUsername(username);
//        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
//            Optional<UserFile> optionalUserFile = userFileRepo.findById(fileId);
//            if (optionalUserFile.isPresent() && optionalUserFile.get().getUser().equals(username)) {
//                return ResponseEntity.ok(optionalUserFile.get());
//            } else {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//            }
//        }
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//    }
//
//    @PutMapping("/file")
//    public ResponseEntity<?> updateFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int fileId, @RequestParam("newname") String newFileName) {
//        Optional<User> optionalUser = userRepo.findByUsername(username);
//        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
//            Optional<UserFile> optionalUserFile = userFileRepo.findById(fileId);
//            if (optionalUserFile.isPresent() && optionalUserFile.get().getUser().equals(username)) {
//                UserFile userFile = optionalUserFile.get();
//                String oldFolderName = userFile.getFileName();
//                userFile.setFileName(newFileName);
//                String oldPath = userFile.getPath();
//                userFile.setPath(fileService.changeFilePath(oldPath, newFileName));
//
//                if (fileService.renameFile(oldPath, newFileName)) {
//                    return ResponseEntity.ok(userFileRepo.save(userFile));
//                }
//            } else {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this file");
//            }
//        }
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or user not found");
//    }

//    @PutMapping("/movefile")
//    public ResponseEntity<?> moveFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("fileid") int fileId, @RequestParam("folderid") int folderId) {
//        Optional<User> optionalUser = userRepo.findByUsername(username);
//        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
//            Optional<UserFile> optionalUserFile = userFileRepo.findById(fileId);
//            Optional<Folder> optionalFolder = folderRepo.findById(folderId);
//            if (optionalUserFile.isPresent() && optionalFolder.isPresent()) {
//                UserFile userFile = optionalUserFile.get();
//                Folder newFolder = optionalFolder.get();
//                if (userFile.getUser().equals(username) && newFolder.getUser().equals(username)) {
//                    Optional<Folder> optionalOldFolder = folderRepo.findById(userFile.getFolderId());
//                    if (optionalOldFolder.isPresent() && optionalOldFolder.get().getUser().equals(username)) {
//                        Folder oldFolder = optionalOldFolder.get();
//                        List<UserFile> oldUserFiles = oldFolder.getFiles();
//                        oldUserFiles.remove(userFile);
//                        oldFolder.setFiles(oldUserFiles);
//                        folderRepo.save(oldFolder);
//
//                        List<UserFile> newUserFiles = newFolder.getFiles();
//                        newUserFiles.add(userFile);
//                        newFolder.setFiles(newUserFiles);
//                        folderRepo.save(newFolder);
//
//                        String oldPath = userFile.getPath();
//                        userFile.setFolderId(newFolder.getId());
//                        userFile.setPath(newFolder.getPath() + "/" + userFile.getFileName());
//                        userFileRepo.save(userFile);
//
//                        if (fileService.moveFile(oldPath, userFile.getPath())) {
//                            return ResponseEntity.ok().build();
//                        } else {
//                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Verschieben der Datei");
//                        }
//                    } else {
//                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Alter Ordner nicht gefunden oder Benutzer nicht berechtigt");
//                    }
//                } else {
//                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Benutzer nicht berechtigt");
//                }
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Datei oder neuer Ordner nicht gefunden");
//            }
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Benutzer nicht gefunden oder Token ungültig");
//        }
//    }
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
