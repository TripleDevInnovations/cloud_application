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
@RequestMapping("/{username}")
@RequiredArgsConstructor
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

    private final static String basePath = "C:/Users/Jacko/Documents/cloudstorage/";

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
        List<UserFile> files = new ArrayList<>();
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
        folder.setPath(basePath + username + "/" + folderName);
        folder.setUser(username);
        List<Folder> folders = new ArrayList<>();
        folders.add(folder);
        user.setFolders(folders);

        fileService.createFolder(folderName, username);
        return userRepo.save(user);
    }

    @PutMapping("/file")
    public UserFile renameFile(@PathVariable String username, @RequestParam("id") int id, @RequestParam("newname") String newName) {
        UserFile userFile = userFileRepo.getReferenceById(id);
        String oldFolderName = userFile.getFileName();
        userFile.setFileName(newName);
        userFile.setPath(basePath + username + "/" + newName);

        fileService.renameFile(username + "/" + oldFolderName, newName);
        return userFileRepo.save(userFile);
        //PathUpdate fehlt
    }
}
