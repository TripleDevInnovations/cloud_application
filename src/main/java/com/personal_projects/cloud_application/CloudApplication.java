package com.personal_projects.cloud_application;

import com.personal_projects.cloud_application.backend.entities.Role;
import com.personal_projects.cloud_application.backend.entities.User;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class CloudApplication implements CommandLineRunner {

    @Autowired private UserRepo userRepo;

    public static void main(String[] args) {
        SpringApplication.run(CloudApplication.class, args);
    }

    public void run(String... args) {
        User ownerUser = userRepo.findByRole(Role.OWNER);
        if (ownerUser == null) {
            User user = new User();
            user.setUsername("owner");
            user.setRole(Role.OWNER);
            user.setPassword(new BCryptPasswordEncoder().encode("owner"));
            userRepo.save(user);
        }
    }
}
