package com.personal_projects.cloud_application.backend.controller;

import com.personal_projects.cloud_application.backend.repositories.UserFileRepo;
import com.personal_projects.cloud_application.backend.repositories.FolderRepo;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import com.personal_projects.cloud_application.backend.services.UserService;
import com.personal_projects.cloud_application.backend.services.FileService;
import com.personal_projects.cloud_application.backend.entities.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

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

//
//    @DeleteMapping("/deletefile")
//    public User deleteFile(@PathVariable String username, @RequestParam("id") int id) {
//        UserFile userFile = userFileRepo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Datei nicht gefunden"));
//        User user = userRepo.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));
//
//        if (!username.equals(userFile.getUser())) {
//            throw new RuntimeException("Datei gehört nicht zum Benutzer");
//        }
//
//        // Entferne die Datei aus der Liste des Benutzers
//        user.getFiles().removeIf(file -> file.getId() == id);
//
//        // Lösche die Datei vom Dateisystem
//        boolean response = fileService.deleteFile(userFile.getPath());
//        if (response) {
//            throw new RuntimeException("Fehler beim Löschen der Datei vom Dateisystem");
//        }
//
//        // Speichere den Benutzer ohne die Datei
//        userRepo.save(user);
//
//        // Lösche die Datei aus der Datenbank
//        userFileRepo.delete(userFile);
//
//        return user;
//    }

//    @DeleteMapping("deleteFile")
//    public User deleteFile(@PathVariable String username, ) {
//
//    }

}
