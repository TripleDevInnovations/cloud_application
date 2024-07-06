package com.personal_projects.cloud_application.backend.services.impl;

import com.personal_projects.cloud_application.backend.repositories.UserFileRepo;
import com.personal_projects.cloud_application.backend.repositories.FolderRepo;
import com.personal_projects.cloud_application.backend.services.FolderService;
import com.personal_projects.cloud_application.backend.services.FileService;
import com.personal_projects.cloud_application.backend.entities.UserFile;
import com.personal_projects.cloud_application.backend.entities.Folder;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@Service
public class FolderServiceImpl implements FolderService {

    @Autowired
    private FolderRepo folderRepo;

    @Autowired
    private UserFileRepo userFileRepo;

    @Autowired
    private FileService fileService;

    @Override
    @Transactional
    public void deleteFolder(Folder folder) {
        if (folder != null) {
            List<Folder> folders = folder.getFolders();
            List<UserFile> files = folder.getFiles();

            folderRepo.delete(folder);

            if (files != null && !files.isEmpty()) {
                for (UserFile file : files) {
                    fileService.deleteFile(file.getId() + "." + fileService.getFileExtension(file.getFileName()));
                    userFileRepo.delete(file);
                }
            }

            if (folders != null && !folders.isEmpty()) {
                for (Folder O : folders) {
                    deleteFolder(O);
                }
            }
        }
    }

    @Override
    public ResponseEntity<?> updateFolderName(Folder folder, String newFolderName) {
        folder.setFolderName(newFolderName);
        try {
            folderRepo.save(folder);
            return ResponseEntity.ok(folder);
        } catch (DataAccessException dae) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Speichern des Ordners.");
        }
    }

    @Override
    public ResponseEntity<?> moveFolderToNewParent(Folder folder, Folder newParentFolder, int userId) {
        if (newParentFolder.getUserId() == userId && folder.getUserId() == userId) {
            Optional<Folder> optionalOldParentFolder = folderRepo.findById(folder.getParentFolderId());
            if (optionalOldParentFolder.isPresent() && optionalOldParentFolder.get().getUserId() == userId) {
                //delete from folder list from old parent-folder with parentFolderId from old Folder
                Folder oldParentFolder = optionalOldParentFolder.get();
                List<Folder> oldFolders = oldParentFolder.getFolders();
                oldFolders.remove(folder);
                oldParentFolder.setFolders(oldFolders);
                try {
                    folderRepo.save(oldParentFolder);
                    //change path
                    folder.setParentFolderId(newParentFolder.getId());
                    List<Folder> newFolders = newParentFolder.getFolders();
                    newFolders.add(folder);
                    newParentFolder.setFolders(newFolders);
                    folderRepo.save(newParentFolder);
                    return ResponseEntity.ok().build();
                } catch (DataAccessException dae) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Speichern des Ordners.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Alter Elternordner nicht gefunden oder Benutzer nicht berechtigt.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Benutzer nicht berechtigt.");
        }
    }

    @Override
    public ResponseEntity<?> removeFolder(Folder folder, Folder parentFolder) {
        List<Folder> folders = parentFolder.getFolders();
        folders.remove(folder);
        parentFolder.setFolders(folders);
        folderRepo.save(parentFolder);
        try {
            deleteFolder(folder);
            return ResponseEntity.ok().build();
        } catch (DataAccessException dae) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Löschen des Ordners.");
        }
    }
}
