package com.personal_projects.cloud_application.backend.repositories;

import com.personal_projects.cloud_application.backend.entities.UserFile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFileRepo extends JpaRepository<UserFile, Integer> {

}
