package com.personal_projects.cloud_application.backend.controller;

import com.personal_projects.cloud_application.backend.entities.Folder;
import com.personal_projects.cloud_application.backend.entities.User;
import com.personal_projects.cloud_application.backend.entities.UserFile;
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
public class UserFileController {

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
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get()) && !file.isEmpty()) {
            Optional<Folder> optionalFolder = folderRepo.findById(folderId);
            if(optionalFolder.isPresent() && optionalFolder.get().getUser().equals(username)) {
                User user = optionalUser.get();
                Folder folder = optionalFolder.get();

                UserFile userFile = new UserFile();
                userFile.setFileName(file.getOriginalFilename());
                userFile.setFileType(file.getContentType());
                userFile.setSize(file.getSize());
                userFile.setPath(folder.getPath() + "/" + userFile.getFileName());
                userFile.setUser(username);

                if (fileService.saveFile(file, userFile.getPath())) {
                    List<UserFile> files = folder.getFiles();
                    files.add(userFile);
                    folder.setFiles(files);
                    folderRepo.save(folder);
                    return ResponseEntity.ok(user);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File could not be saved");
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this folder");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or file is empty");
    }

    @GetMapping("/file")
    public ResponseEntity<UserFile> readFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int fileId) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
            Optional<UserFile> optionalUserFile = userFileRepo.findById(fileId);
            if (optionalUserFile.isPresent() && optionalUserFile.get().getUser().equals(username)) {
                return ResponseEntity.ok(optionalUserFile.get());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping("/file")
    public ResponseEntity<?> updateFile(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int fileId, @RequestParam("newname") String newFileName) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
            Optional<UserFile> optionalUserFile = userFileRepo.findById(fileId);
            if (optionalUserFile.isPresent() && optionalUserFile.get().getUser().equals(username)) {
                UserFile userFile = optionalUserFile.get();
                String oldFolderName = userFile.getFileName();
                userFile.setFileName(newFileName);
                String oldPath = userFile.getPath();
                userFile.setPath(fileService.changeFilePath(oldPath, newFileName));

                fileService.renameFile(oldPath, newFileName);
                return ResponseEntity.ok(userFileRepo.save(userFile));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this file");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or user not found");
    }
}
