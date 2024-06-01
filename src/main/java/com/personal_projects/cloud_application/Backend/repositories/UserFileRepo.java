package com.personal_projects.cloud_application.Backend.repositories;

import com.personal_projects.cloud_application.Backend.entities.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFileRepo extends JpaRepository<UserFile, Integer> {
}
