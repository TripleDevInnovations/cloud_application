package com.personal_projects.cloud_application.backend.services;

import com.personal_projects.cloud_application.backend.entities.Folder;
import org.springframework.http.ResponseEntity;

public interface FolderService {

    void deleteFolder(Folder folder);

    ResponseEntity<?> updateFolderName(Folder folder, String newFolderName);

    ResponseEntity<?> moveFolderToNewParent(Folder folder, Folder newParentFolder, int UserId);

    ResponseEntity<?> removeFolder(Folder folder, Folder parentFolder);
}
