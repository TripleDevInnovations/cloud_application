package com.personal_projects.cloud_application;

import com.personal_projects.cloud_application.backend.repositories.FolderRepo;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import com.personal_projects.cloud_application.backend.services.FileService;
import com.personal_projects.cloud_application.backend.entities.Folder;
import com.personal_projects.cloud_application.backend.entities.Role;
import com.personal_projects.cloud_application.backend.entities.User;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

@SpringBootApplication
public class CloudApplication implements CommandLineRunner {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private FolderRepo folderRepo;
    @Autowired
    private FileService fileService;

    public static void main(String[] args) {
        SpringApplication.run(CloudApplication.class, args);
    }

    @Override
    public void run(String... args) {
        User ownerUser = userRepo.findByRole(Role.OWNER);
        if (ownerUser == null) {
            User user = new User();
            user.setUsername("owner");
            user.setRole(Role.OWNER);
            user.setPassword(new BCryptPasswordEncoder().encode("owner"));
            user = userRepo.save(user);

            Folder rootFolder = new Folder();
            rootFolder.setFolderName(user.getUsername());
            rootFolder.setUserId(user.getId());
            rootFolder.setParentFolderId(0);
            rootFolder = folderRepo.save(rootFolder);

            user.setRootFolder(rootFolder);

            // Speichere den Benutzer und den Ordner in der Datenbank
            user = userRepo.save(user);

            fileService.createFolder(String.valueOf(user.getId()), "");
        }
    }
}
