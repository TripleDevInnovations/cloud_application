package com.personal_projects.cloud_application.backend.controller;

import com.personal_projects.cloud_application.backend.entities.Folder;
import com.personal_projects.cloud_application.backend.entities.User;
import com.personal_projects.cloud_application.backend.entities.UserFile;
import com.personal_projects.cloud_application.backend.repositories.FolderRepo;
import com.personal_projects.cloud_application.backend.repositories.UserFileRepo;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.personal_projects.cloud_application.backend.services.FileService;
import com.personal_projects.cloud_application.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{username}")
public class UserController {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private FolderRepo folderRepo;
    @Autowired
    private UserFileRepo userFileRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private FileService fileService;

    private final static String basePath = "C:/Users/JAHEESE/Documents/cloudstorage/";

    @GetMapping
    public Optional<User> readUser(@PathVariable String username) {
        return userRepo.findByUsername(username);
        //return userService.loadUserByUsername(username);
        //userRepo returned null, Gefährlich -> IF (user == null) -> ...
    }

    @PostMapping("/createfile")
    public User createFile(@PathVariable String username, @RequestParam("file") MultipartFile file) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (file.isEmpty() || optionalUser.isEmpty()) {
            return null;
        }
        User user = optionalUser.get();
        UserFile userFile = new UserFile();
        userFile.setFileName(file.getOriginalFilename());
        userFile.setFileType(file.getContentType());
        userFile.setSize(file.getSize());
        userFile.setPath(basePath + username + "/" + userFile.getFileName());
        userFile.setUser(username);
        List<UserFile> files = user.getFiles();
        files.add(userFile);
        user.setFiles(files);

        fileService.saveFile(file, username);
        return userRepo.save(user);
    }

    @PostMapping("/createfolder")
    public User createFolder(@PathVariable String username, @RequestParam("folder") String folderName) {
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (folderName.isEmpty() || optionalUser.isEmpty()) {
            return null;
        }
        User user = optionalUser.get();
        Folder folder = new Folder();
        folder.setFolderName(folderName);
        folder.setPath(username + "/" + folderName);
        folder.setUser(username);
        List<Folder> folders = new ArrayList<>();
        folders.add(folder);
        user.setFolders(folders);

        fileService.createFolder(folderName, username);
        return userRepo.save(user);
    }

    @PutMapping
    public UserFile renameFile(@PathVariable String username, @RequestParam("id") int id, @RequestParam("newname") String newName) {
        UserFile userFile = userFileRepo.getReferenceById(id);
        String oldFolderName = userFile.getFileName();
        userFile.setFileName(newName);
        userFile.setPath(basePath + username + "/" + newName);

        fileService.renameFile(username + "/" + oldFolderName, newName);
        return userFileRepo.save(userFile);
        //PathUpdate fehlt
    }

    @DeleteMapping("/deletefile")
    public User deleteFile(@PathVariable String username, @RequestParam("id") int id) {
        UserFile userFile = userFileRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Datei nicht gefunden"));
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));

        if (!username.equals(userFile.getUser())) {
            throw new RuntimeException("Datei gehört nicht zum Benutzer");
        }

        // Entferne die Datei aus der Liste des Benutzers
        user.getFiles().removeIf(file -> file.getId() == id);

        // Lösche die Datei vom Dateisystem
        String response = fileService.deleteFile(userFile.getPath());
        if (!response.equals("Datei erfolgreich gelöscht")) {
            throw new RuntimeException("Fehler beim Löschen der Datei vom Dateisystem");
        }

        // Speichere den Benutzer ohne die Datei
        userRepo.save(user);

        // Lösche die Datei aus der Datenbank
        userFileRepo.delete(userFile);

        return user;
    }

//    @DeleteMapping("deleteFile")
//    public User deleteFile(@PathVariable String username, ) {
//
//    }

}
