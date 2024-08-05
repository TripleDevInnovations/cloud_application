package com.personal_projects.cloud_application.backend.controller;

import com.personal_projects.cloud_application.backend.entities.UserFile;
import com.personal_projects.cloud_application.backend.repositories.UserFileRepo;
import com.personal_projects.cloud_application.backend.repositories.FolderRepo;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import com.personal_projects.cloud_application.backend.services.FileService;
import com.personal_projects.cloud_application.backend.services.JWTService;
import com.personal_projects.cloud_application.backend.entities.Folder;
import com.personal_projects.cloud_application.backend.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
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
    public ResponseEntity<?> createFolder(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int parentFolderId, @RequestParam("foldername") String folderName) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
            Optional<Folder> optionalFolder = folderRepo.findById(parentFolderId);
            int userId = optionalUser.get().getId();
            if (optionalFolder.isPresent() && optionalFolder.get().getUserId() == userId) {
                Folder folder = optionalFolder.get();
                List<Folder> folders = folder.getFolders();

                Folder newFolder = new Folder();
                newFolder.setFolderName(folderName);
                newFolder.setUserId(userId);
                newFolder.setParentFolderId(folder.getId());
                newFolder = folderRepo.save(newFolder);
                folders.add(newFolder);
                folder.setFolders(folders);
                folderRepo.save(folder);

                return ResponseEntity.ok().build();
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

    @PutMapping("/folder")
    public ResponseEntity<?> updateFolder(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int folderId, @RequestParam("newname") String newFolderName) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
            Optional<Folder> optionalFolder = folderRepo.findById(folderId);
            int userId = optionalUser.get().getId();
            if (optionalFolder.isPresent() && optionalFolder.get().getUserId() == userId) {
                Folder folder = optionalFolder.get();
                if (folder.getParentFolderId() != 0) {
                    folder.setFolderName(newFolderName);
                    folderRepo.save(folder);
                    return ResponseEntity.ok(optionalFolder.get());
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Dieser Ordner darf nicht umbenannt werden.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Übergeordneter Ordner nicht gefunden oder Benutzer nicht berechtigt");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Benutzer nicht gefunden oder Token ungültig");
        }
    }

    @PutMapping("/movefolder")
    public ResponseEntity<?> moveFolder(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int id, @RequestParam("moveid") int newParentId) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
            Optional<Folder> optionalFolder = folderRepo.findById(id);
            Optional<Folder> optionalNewFolder = folderRepo.findById(newParentId);
            int userId = optionalUser.get().getId();
            if (optionalFolder.isPresent() && optionalNewFolder.isPresent() && optionalFolder.get().getUserId() == userId && optionalNewFolder.get().getUserId() == userId) {
                Folder folder = optionalFolder.get();
                    if (folder.getParentFolderId() != 0) {
                        Folder newFolder = optionalNewFolder.get();
                        Optional<Folder> optionalOldParentFolder = folderRepo.findById(folder.getParentFolderId());
                        if (optionalOldParentFolder.isPresent() && optionalOldParentFolder.get().getUserId() == userId) {
                            //delete from folder list from old parent-folder with parentFolderId from old Folder
                            Folder oldParentFolder = optionalOldParentFolder.get();
                            List<Folder> oldFolders = oldParentFolder.getFolders();
                            oldFolders.remove(folder);
                            oldParentFolder.setFolders(oldFolders);
                            folderRepo.save(oldParentFolder);
                            //change path
                            folder.setParentFolderId(newFolder.getId());
                            List<Folder> newFolders = newFolder.getFolders();
                            newFolders.add(folder);
                            newFolder.setFolders(newFolders);
                            folderRepo.save(newFolder);
                            return ResponseEntity.ok().build();
                        } else {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Alter Elternordner nicht gefunden oder Benutzer nicht berechtigt");
                        }
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Dieser Ordner darf nicht verschoben werden.");
                    }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ordner oder neuer Elternordner nicht gefunden oder Benutzer nicht berechtigt");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Benutzer nicht gefunden oder Token ungültig");
        }
    }

    @DeleteMapping("/folder")
    public ResponseEntity<?> deleteFolder(@PathVariable String username, @RequestHeader("Authorization") String token, @RequestParam("id") int id) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent() && jwtService.isTokenValid(token, optionalUser.get())) {
            Optional<Folder> optionalFolder = folderRepo.findById(id);
            if (optionalFolder.isPresent() && optionalFolder.get().getUserId() == optionalUser.get().getId()) {
                Folder folder = optionalFolder.get();
                if (folder.getParentFolderId() != 0) {
                    deleteSubFolders(folder);
                    folderRepo.delete(folder);
                    return ResponseEntity.ok().build();
                } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Dieser Ordner darf nicht gelöscht werden.");
                }
            } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ordner nicht gefunden oder Benutzer nicht berechtigt");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Benutzer nicht gefunden oder Token ungültig");
        }
    }

    private void deleteSubFolders(Folder folder) {
        if (folder != null) {
            boolean saveFolder = false;
            List<Folder> folders = folder.getFolders();
            List<UserFile> files = folder.getFiles();

            if (!files.isEmpty()) {
                for (UserFile file : files) {
                    fileService.deleteFile(file.getId() + "." + fileService.getFileExtension(file.getFileName()));
                    userFileRepo.delete(file);
                }
                folder.setFiles(new ArrayList<>());
                saveFolder = true;
            }

            if (!folders.isEmpty()) {
                for (Folder O : folders) {
                    deleteSubFolders(O);
                    folderRepo.delete(O);
                }
                folder.setFolders(new ArrayList<>());
                saveFolder = true;
            }

            if (saveFolder) {
                folderRepo.save(folder);
            }
        }
    }
}














