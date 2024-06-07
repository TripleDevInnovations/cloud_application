package com.personal_projects.cloud_application.backend.controller;

import com.personal_projects.cloud_application.backend.entities.User;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
//@RequestMapping("/{username}")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserRepo userRepo;

    @GetMapping
    public Optional<User> getUser(@PathVariable String username) {
        return userRepo.findByUsername(username);
    }

}
