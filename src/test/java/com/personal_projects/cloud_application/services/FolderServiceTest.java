package com.personal_projects.cloud_application.services;

import com.personal_projects.cloud_application.backend.entities.Folder;
import com.personal_projects.cloud_application.backend.entities.UserFile;
import com.personal_projects.cloud_application.backend.repositories.FolderRepo;
import com.personal_projects.cloud_application.backend.repositories.UserFileRepo;
import com.personal_projects.cloud_application.backend.services.FileService;
import com.personal_projects.cloud_application.backend.services.impl.FolderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FolderServiceTest {

    @Mock
    private FolderRepo folderRepo;

    @Mock
    private UserFileRepo userFileRepo;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FolderServiceImpl folderService;

    private Folder folder;
    private Folder parentFolder;

    @BeforeEach
    public void setUp() {
        folder = new Folder();
        folder.setId(1);
        folder.setFolderName("TestFolder");

        parentFolder = new Folder();
        parentFolder.setId(2);
        parentFolder.setFolderName("ParentFolder");

        UserFile userFile = new UserFile();
        userFile.setId(1);
        userFile.setFileName("test.txt");

        List<UserFile> files = new ArrayList<>();
        files.add(userFile);
        folder.setFiles(files);

        List<Folder> folders = new ArrayList<>();
        folders.add(folder);
        parentFolder.setFolders(folders);
    }

    @Test
    public void testDeleteFolder() {
        Folder folderToDelete = new Folder();
        List<Folder> folders = new ArrayList<>();
        Folder testFolder = new Folder();
        testFolder.setId(1);
        folders.add(testFolder);
        folderToDelete.setFolders(folders);
        List<UserFile> files = new ArrayList<>();
        UserFile testUserFile = new UserFile();
        testUserFile.setId(1);
        testUserFile.setFileName("test.txt");
        files.add(testUserFile);
        folderToDelete.setFiles(files);

        doNothing().when(folderRepo).delete(any(Folder.class));
        doNothing().when(userFileRepo).delete(any(UserFile.class));
        when(fileService.deleteFile(anyString())).thenReturn(true);
        when(fileService.getFileExtension(anyString())).thenReturn("txt");

        folderService.deleteFolder(folderToDelete);

        verify(folderRepo, times(2)).delete(any(Folder.class));
        verify(userFileRepo, times(1)).delete(testUserFile);
        verify(fileService, times(1)).deleteFile("1.txt");
    }

    @Test
    public void testUpdateFolderName() {
        when(folderRepo.save(any(Folder.class))).thenReturn(folder);

        ResponseEntity<?> response = folderService.updateFolderName(folder, "NewFolderName");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("NewFolderName", folder.getFolderName());
    }

    @Test
    public void testUpdateFolderName_DataAccessException() {
        when(folderRepo.save(any(Folder.class))).thenThrow(new DataAccessException("...") {});

        ResponseEntity<?> response = folderService.updateFolderName(folder, "NewFolderName");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Fehler beim Speichern des Ordners.", response.getBody());
    }

    @Test
    public void testMoveFolderToNewParent() {
        Folder testFolder = new Folder();
        testFolder.setUserId(1);
        Folder testParentFolder = new Folder();
        testParentFolder.setUserId(1);
        List<Folder> testFolders = new ArrayList<>();
        testFolders.add(testFolder);
        testParentFolder.setFolders(testFolders);


        when(folderRepo.findById(anyInt())).thenReturn(Optional.of(testParentFolder));
        when(folderRepo.save(any(Folder.class))).thenReturn(testParentFolder);

        ResponseEntity<?> response = folderService.moveFolderToNewParent(testFolder, testParentFolder, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testMoveFolderToNewParent_Forbidden() {
        ResponseEntity<?> response = folderService.moveFolderToNewParent(folder, parentFolder, 1);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Benutzer nicht berechtigt.", response.getBody());
    }

    @Test
    public void testRemoveFolder() {
        when(folderRepo.save(any(Folder.class))).thenReturn(parentFolder);
        doNothing().when(folderRepo).delete(any(Folder.class));

        ResponseEntity<?> response = folderService.removeFolder(folder, parentFolder);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(folderRepo, times(1)).save(parentFolder);
        verify(folderRepo, times(1)).delete(folder);
    }

    @Test
    public void testRemoveFolder_DataAccessException() {
        when(folderRepo.save(any(Folder.class))).thenReturn(parentFolder);
        doThrow(new DataAccessException("...") {}).when(folderRepo).delete(any(Folder.class));

        ResponseEntity<?> response = folderService.removeFolder(folder, parentFolder);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Fehler beim Löschen des Ordners.", response.getBody());
    }
}