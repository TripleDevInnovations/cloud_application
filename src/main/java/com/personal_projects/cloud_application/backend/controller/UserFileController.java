package com.personal_projects.cloud_application.backend.controller;

import com.personal_projects.cloud_application.backend.entities.User;
import com.personal_projects.cloud_application.backend.entities.UserFile;
import com.personal_projects.cloud_application.backend.repositories.UserFileRepo;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import com.personal_projects.cloud_application.backend.services.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{username}")
public class UserFileController {

    @Autowired
    private JWTService jwtService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserFileRepo userFileRepo;

    @GetMapping("/file")
    public ResponseEntity<UserFile> readFile(@PathVariable String username, @RequestParam("id") int id, @RequestHeader("Authorization") String token) {
        Optional<User> user = userRepo.findByUsername(username);
        if (user.isPresent() && jwtService.isTokenValid(token, user.get())) {
            Optional<UserFile> userFile = userFileRepo.findById(id);
            if (userFile.isPresent() && userFile.get().getUser().equals(username)) {
                return ResponseEntity.ok(userFile.get());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
