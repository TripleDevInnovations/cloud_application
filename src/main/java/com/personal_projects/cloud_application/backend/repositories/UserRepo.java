package com.personal_projects.cloud_application.backend.repositories;

import com.personal_projects.cloud_application.backend.entities.Role;
import com.personal_projects.cloud_application.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    default User findUserByName(List<User> users, String name) {
        for (User user : users) {
            if (user.getUsername() != null && user.getUsername().equals(name)) {
                return user;
            }
        }
        return null;
    }

    User findByRole(Role role);

    //ERGÄNZUNGEN IM TEST FÜR DIESES REPO/KLASSE
}
