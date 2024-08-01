package com.personal_projects.cloud_application.controller;
import static org.hamcrest.CoreMatchers.is;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal_projects.cloud_application.backend.controller.AuthenticationController;
import com.personal_projects.cloud_application.backend.controller.FolderController;
import com.personal_projects.cloud_application.backend.entities.Folder;
import com.personal_projects.cloud_application.backend.entities.User;
import com.personal_projects.cloud_application.backend.repositories.FolderRepo;
import com.personal_projects.cloud_application.backend.repositories.UserFileRepo;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import com.personal_projects.cloud_application.backend.services.*;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = FolderController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class FolderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private UserRepo userRepo;

    @MockBean
    private FolderRepo folderRepo;

    @MockBean
    private UserFileRepo userFileRepo;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private CommunicationService communicationService;

    @MockBean
    private FileService fileService;

    @Test
    public void folderController_crateFolder_ReturnsUnauthorized() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());
        given(fileService.createFolder(anyString(), anyString())).willReturn(true);

        ResultActions response = mockMvc.perform(post("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("foldername", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig"));
    }

    @Test
    public void folderController_crateFolder_ReturnsUnauthorized2() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(false);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());
        given(fileService.createFolder(anyString(), anyString())).willReturn(true);

        ResultActions response = mockMvc.perform(post("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("foldername", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig"));
    }

    @Test
    public void folderController_crateFolder_ReturnsNotFound() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());
        given(fileService.createFolder(anyString(), anyString())).willReturn(true);

        ResultActions response = mockMvc.perform(post("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("foldername", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Übergeordneter Ordner nicht gefunden oder Benutzer nicht berechtigt"));
    }

    @Test
    public void folderController_crateFolder_ReturnsNotFound2() throws Exception {
        User user = new User();
        user.setId(1);
        Folder folder = new Folder();
        folder.setUserId(2);
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));
        given(fileService.createFolder(anyString(), anyString())).willReturn(true);

        ResultActions response = mockMvc.perform(post("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("foldername", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Übergeordneter Ordner nicht gefunden oder Benutzer nicht berechtigt"));
    }

    @Test
    public void folderController_crateFolder_ReturnsInternalServerError() throws Exception {
        User mockUser = new User();
        List<Folder> folders = new ArrayList<>();
        Folder mockFolder = new Folder();
        mockFolder.setFolders(folders);
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(mockUser));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(mockFolder));
        given(fileService.createFolder(anyString(), anyString())).willReturn(false);

        ResultActions response = mockMvc.perform(post("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("foldername", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(content().string("Fehler beim Erstellen des Ordners im Dateisystem"));
    }

    @Test
    public void folderController_crateFolder_ReturnsOK() throws Exception {
        User mockUser = new User();
        List<Folder> folders = new ArrayList<>();
        Folder mockFolder = new Folder();
        mockFolder.setFolders(folders);
        mockFolder.setPath("");
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(mockUser));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(mockFolder));
        given(fileService.createFolder(anyString(), anyString())).willReturn(true);

        ResultActions response = mockMvc.perform(post("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("foldername", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }




    @Test
    public void folderController_readFolder_ReturnsUnauthorized() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(null);

        ResultActions response = mockMvc.perform(get("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig"));
    }

    @Test
    public void folderController_readFolder_ReturnsUnauthorized2() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(false);
        given(folderRepo.findById(anyInt())).willReturn(null);

        ResultActions response = mockMvc.perform(get("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig"));
    }

    @Test
    public void folderController_readFolder_ReturnsNotFound() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(get("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Ordner nicht gefunden oder Benutzer nicht berechtigt"));
    }

    @Test
    public void folderController_readFolder_ReturnsNotFound2() throws Exception {
        User user = new User();
        user.setId(1);
        Folder folder = new Folder();
        folder.setUserId(3);
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));

        ResultActions response = mockMvc.perform(get("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Ordner nicht gefunden oder Benutzer nicht berechtigt"));
    }

    @Test
    public void folderController_readFolder_ReturnsOK() throws Exception {
        User user = new User();
        user.setId(1);
        Folder folder = new Folder();
        folder.setUserId(1);
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));

        ResultActions response = mockMvc.perform(get("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.parentFolderId").value(0));
    }




    @Test
    public void folderController_updateFolder_ReturnsUnauthorized() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());
        given(fileService.renameFile(anyString(), anyString())).willReturn(true);

        ResultActions response = mockMvc.perform(put("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("newname", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig"));
    }

    @Test
    public void folderController_updateFolder_ReturnsUnauthorized2() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(false);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());
        given(fileService.renameFile(anyString(), anyString())).willReturn(true);

        ResultActions response = mockMvc.perform(put("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("newname", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig"));
    }

    @Test
    public void folderController_updateFolder_ReturnsNotFound() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());
        given(fileService.renameFile(anyString(), anyString())).willReturn(true);

        ResultActions response = mockMvc.perform(put("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("newname", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Übergeordneter Ordner nicht gefunden oder Benutzer nicht berechtigt"));
    }

    @Test
    public void folderController_updateFolder_ReturnsNotFound2() throws Exception {
        User user = new User();
        user.setId(1);
        Folder folder = new Folder();
        folder.setUserId(3);
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));
        given(fileService.renameFile(anyString(), anyString())).willReturn(true);

        ResultActions response = mockMvc.perform(put("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("newname", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Übergeordneter Ordner nicht gefunden oder Benutzer nicht berechtigt"));
    }

    @Test
    public void folderController_updateFolder_ReturnsInternalServerError() throws Exception {
        User user = new User();
        user.setId(1);
        Folder folder = new Folder();
        folder.setUserId(1);
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));
        given(fileService.renameFile(anyString(), anyString())).willReturn(false);

        ResultActions response = mockMvc.perform(put("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("newname", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(content().string("Fehler beim umbenennen des Ordners im Dateisystem"));
    }

    @Test
    public void folderController_updateFolder_ReturnsOk() throws Exception {
        User user = new User();
        user.setId(1);
        Folder folder = new Folder();
        folder.setUserId(1);
        folder.setPath("");
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));
        given(fileService.renameFile(anyString(), anyString())).willReturn(true );
        given(fileService.changeFilePath(anyString(), anyString())).willReturn("testPath");

        ResultActions response = mockMvc.perform(put("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("newname", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.path").value("testPath"));
    }




    @Test
    public void folderController_moveFolder_ReturnsUnauthorized() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());
        given(fileService.renameFile(anyString(), anyString())).willReturn(true );
        given(fileService.changeFilePath(anyString(), anyString())).willReturn("testPath");

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("moveid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig"));
    }

    @Test
    public void folderController_moveFolder_ReturnsUnauthorized2() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(false);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());
        given(fileService.renameFile(anyString(), anyString())).willReturn(true );
        given(fileService.changeFilePath(anyString(), anyString())).willReturn("testPath");

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("moveid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig"));
    }

    @Test
    public void folderController_moveFolder_ReturnsNotFound() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());
        given(fileService.renameFile(anyString(), anyString())).willReturn(true );
        given(fileService.changeFilePath(anyString(), anyString())).willReturn("testPath");

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("moveid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Ordner oder neuer Elternordner nicht gefunden oder Benutzer nicht berechtigt"));
    }

    @Test
    public void folderController_moveFolder_ReturnsNotFound2() throws Exception {
        User user = new User();
        user.setId(1);
        Folder folder = new Folder();
        folder.setUserId(3);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));
        given(fileService.renameFile(anyString(), anyString())).willReturn(true );

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("moveid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Ordner oder neuer Elternordner nicht gefunden oder Benutzer nicht berechtigt"));
    }

    @Test
    public void folderController_moveFolder_ReturnsForbidden() throws Exception {
        User user = new User();
        user.setId(1);

        Folder folder = new Folder();
        folder.setId(1);
        folder.setUserId(1);
        folder.setParentFolderId(2);
        folder.setPath("oldPath");

        Folder newParentFolder = new Folder();
        newParentFolder.setId(3);
        newParentFolder.setUserId(1);
        newParentFolder.setPath("newPath");

        Folder oldParentFolder = new Folder();
        oldParentFolder.setId(2);
        oldParentFolder.setUserId(1);
        oldParentFolder.setPath("oldParentPath");

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);

        given(folderRepo.findById(1)).willReturn(Optional.of(folder));
        given(folderRepo.findById(3)).willReturn(Optional.of(newParentFolder));
        given(folderRepo.findById(2)).willReturn(Optional.empty());

        given(fileService.moveFile(anyString(), anyString())).willReturn(true);

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("moveid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().string("Alter Elternordner nicht gefunden oder Benutzer nicht berechtigt"));
    }

    @Test
    public void folderController_moveFolder_ReturnsForbidden2() throws Exception {
        User user = new User();
        user.setId(1);

        Folder folder = new Folder();
        folder.setId(1);
        folder.setUserId(1);
        folder.setParentFolderId(2);
        folder.setPath("oldPath");

        Folder newParentFolder = new Folder();
        newParentFolder.setId(3);
        newParentFolder.setUserId(1);
        newParentFolder.setPath("newPath");

        Folder oldParentFolder = new Folder();
        oldParentFolder.setId(2);
        oldParentFolder.setUserId(2);
        oldParentFolder.setPath("oldParentPath");

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);

        given(folderRepo.findById(1)).willReturn(Optional.of(folder));
        given(folderRepo.findById(3)).willReturn(Optional.of(newParentFolder));
        given(folderRepo.findById(2)).willReturn(Optional.of(oldParentFolder));

        given(fileService.moveFile(anyString(), anyString())).willReturn(true);

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("moveid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().string("Alter Elternordner nicht gefunden oder Benutzer nicht berechtigt"));
    }

    @Test
    public void folderController_moveFolder_ReturnsInternalServerError() throws Exception {
        User user = new User();
        user.setId(1);

        Folder folder = new Folder();
        folder.setUserId(1);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));
        given(fileService.moveFile(anyString(), anyString())).willReturn(false);

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("moveid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(content().string("Fehler beim Verschieben des Ordners im Dateisystem"));
    }

    @Test
    public void folderController_moveFolder_ReturnsOk() throws Exception {
        User user = new User();
        user.setId(1);

        Folder folder = new Folder();
        folder.setId(1);
        folder.setFolderName("folderName");
        folder.setUserId(1);
        folder.setFolders(new ArrayList<>());
        folder.setPath("path");

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));
        given(fileService.moveFile(anyString(), anyString())).willReturn(true);

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("moveid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
