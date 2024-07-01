package com.personal_projects.cloud_application.backend.controller;

import com.personal_projects.cloud_application.backend.entities.User;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;

import java.util.Optional;

import com.personal_projects.cloud_application.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
// @RequestMapping("/{username}")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserService userService;

    @GetMapping("/{username}")
    public Optional<User> getUser(@PathVariable String username) {
        return userRepo.findByUsername(username);
        //return userService.loadUserByUsername(username);
        //userRepo returned null, Gefährlich -> IF (user == null) -> ...
    }
}
