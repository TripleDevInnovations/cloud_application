package com.personal_projects.cloud_application.backend.repositories;

import com.personal_projects.cloud_application.backend.entities.Folder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepo extends JpaRepository<Folder, Integer> {
}
