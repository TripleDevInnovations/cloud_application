package com.personal_projects.cloud_application.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.dao.DataAccessException;

import com.personal_projects.cloud_application.backend.controller.FolderController;
import com.personal_projects.cloud_application.backend.repositories.UserFileRepo;
import com.personal_projects.cloud_application.backend.repositories.FolderRepo;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import com.personal_projects.cloud_application.backend.entities.Folder;
import com.personal_projects.cloud_application.backend.entities.User;
import com.personal_projects.cloud_application.backend.services.*;
import com.personal_projects.cloud_application.CloudApplication;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;

import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

@WebMvcTest(controllers = FolderController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class FolderControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    private FolderService folderService;

    @MockBean
    private CloudApplication cloudApplication;

    @Test
    public void folderController_CreateFolder_ReturnsBadRequest() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(post("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("foldername", ""));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().string("Ordnername darf nicht leer sein."));
    }

    @Test
    public void folderController_CreateFolder_ReturnsUnauthorized() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(post("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("foldername", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig."));
    }

    @Test
    public void folderController_CreateFolder_ReturnsUnauthorized2() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(false);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(post("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("foldername", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig."));
    }

    @Test
    public void folderController_CreateFolder_ReturnsNotFound() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(post("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("foldername", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Übergeordneter Ordner nicht gefunden oder Benutzer nicht berechtigt."));
    }

    @Test
    public void folderController_CreateFolder_ReturnsNotFound2() throws Exception {
        User user = new User();
        user.setId(1);
        Folder folder = new Folder();
        folder.setUserId(2);
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));

        ResultActions response = mockMvc.perform(post("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("foldername", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Übergeordneter Ordner nicht gefunden oder Benutzer nicht berechtigt."));
    }

    @Test
    public void  folderController_CreateFolder_ReturnsInternalServerError() throws Exception {
        User mockUser = new User();
        List<Folder> folders = new ArrayList<>();
        Folder mockFolder = new Folder();
        mockFolder.setFolders(folders);
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(mockUser));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(mockFolder));
        given(folderRepo.save(any())).willThrow(new DataAccessException("Fehler beim Speichern des Ordners.") {});

        ResultActions response = mockMvc.perform(post("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("foldername", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(content().string("Fehler beim Speichern des Ordners."));
    }

    @Test
    public void folderController_CreateFolder_ReturnsOK() throws Exception {
        User mockUser = new User();
        List<Folder> folders = new ArrayList<>();
        Folder mockFolder = new Folder();
        mockFolder.setFolders(folders);
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(mockUser));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(mockFolder));

        ResultActions response = mockMvc.perform(post("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("foldername", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
//
//
//
//
    @Test
    public void folderController_ReadFolder_ReturnsUnauthorized() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(null);

        ResultActions response = mockMvc.perform(get("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig."));
    }

    @Test
    public void folderController_ReadFolder_ReturnsUnauthorized2() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(false);
        given(folderRepo.findById(anyInt())).willReturn(null);

        ResultActions response = mockMvc.perform(get("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig."));
    }

    @Test
    public void folderController_ReadFolder_ReturnsNotFound() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(get("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Ordner nicht gefunden oder Benutzer nicht berechtigt."));
    }

    @Test
    public void folderController_ReadFolder_ReturnsNotFound2() throws Exception {
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
                .andExpect(content().string("Ordner nicht gefunden oder Benutzer nicht berechtigt."));
    }

    @Test
    public void folderController_ReadFolder_ReturnsOK() throws Exception {
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
    public void folderController_UpdateFolder_ReturnsBadRequest() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(put("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("newname", ""));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().string("Neuer Ordnername darf nicht leer sein."));
    }
    @Test
    public void folderController_UpdateFolder_ReturnsUnauthorized() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(put("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("newname", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig."));
    }

    @Test
    public void folderController_UpdateFolder_ReturnsUnauthorized2() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(false);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(put("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("newname", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig."));
    }

    @Test
    public void folderController_UpdateFolder_ReturnsNotFound() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(put("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("newname", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Übergeordneter Ordner nicht gefunden oder Benutzer nicht berechtigt."));
    }

    @Test
    public void folderController_UpdateFolder_ReturnsNotFound2() throws Exception {
        User user = new User();
        user.setId(1);
        Folder folder = new Folder();
        folder.setUserId(3);
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));

        ResultActions response = mockMvc.perform(put("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("newname", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Übergeordneter Ordner nicht gefunden oder Benutzer nicht berechtigt."));
    }

    @Test
    public void folderController_UpdateFolder_ReturnsForbidden() throws Exception {
        User user = new User();
        user.setId(1);
        Folder folder = new Folder();
        folder.setUserId(1);
        folder.setParentFolderId(0);
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));

        ResultActions response = mockMvc.perform(put("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("newname", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().string("Dieser Ordner darf nicht umbenannt werden."));
    }

    @Test
    public void folderController_UpdateFolder_ReturnsInternalServerError() throws Exception {
        User user = new User();
        user.setId(1);
        Folder folder = new Folder();
        folder.setUserId(1);
        folder.setParentFolderId(1);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));

        given(folderService.updateFolderName(folder, "foldername")).willReturn(new ResponseEntity("Fehler beim Speichern des Ordners.", HttpStatus.INTERNAL_SERVER_ERROR));

        ResultActions response = mockMvc.perform(put("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("newname", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(content().string("Fehler beim Speichern des Ordners."));
    }

    @Test
    public void folderController_UpdateFolder_ReturnsOk() throws Exception {
        User user = new User();
        user.setId(1);
        Folder folder = new Folder();
        folder.setUserId(1);
        folder.setParentFolderId(1);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));
        given(folderService.updateFolderName(any(Folder.class), anyString())).willReturn(new ResponseEntity (HttpStatus.OK));

        ResultActions response = mockMvc.perform(put("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("newname", "foldername"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }




    @Test
    public void folderController_MoveFolder_ReturnsUnauthorized() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("targetid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig."));
    }

    @Test
    public void folderController_MoveFolder_ReturnsUnauthorized2() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(false);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("targetid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig."));
    }

    @Test
    public void folderController_MoveFolder_ReturnsNotFound() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("targetid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Ordner oder neuer Elternordner nicht gefunden oder Benutzer nicht berechtigt."));
    }

    @Test
    public void folderController_MoveFolder_ReturnsNotFound2() throws Exception {
        User user = new User();
        user.setId(1);
        Folder folder = new Folder();
        folder.setUserId(3);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("targetid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Ordner oder neuer Elternordner nicht gefunden oder Benutzer nicht berechtigt."));
    }

    @Test
    public void folderController_MoveFolder_ReturnsForbidden() throws Exception {
        User user = new User();
        user.setId(1);

        Folder folder = new Folder();
        folder.setId(1);
        folder.setUserId(1);
        folder.setParentFolderId(0);

        Folder newParentFolder = new Folder();
        newParentFolder.setId(3);
        newParentFolder.setUserId(1);

        Folder oldParentFolder = new Folder();
        oldParentFolder.setId(2);
        oldParentFolder.setUserId(1);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(1)).willReturn(Optional.of(folder));
        given(folderRepo.findById(3)).willReturn(Optional.of(newParentFolder));
        given(folderRepo.findById(2)).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("targetid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().string("Dieser Ordner darf nicht verschoben werden."));
    }

    @Test
    public void folderController_MoveFolder_ReturnsForbidden2() throws Exception {
        User user = new User();
        user.setId(1);

        Folder folder = new Folder();
        folder.setId(1);
        folder.setUserId(1);
        folder.setParentFolderId(2);

        Folder newParentFolder = new Folder();
        newParentFolder.setId(3);
        newParentFolder.setUserId(1);

        Folder oldParentFolder = new Folder();
        oldParentFolder.setId(2);
        oldParentFolder.setUserId(1);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(1)).willReturn(Optional.of(folder));
        given(folderRepo.findById(3)).willReturn(Optional.of(newParentFolder));
        given(folderRepo.findById(2)).willReturn(Optional.empty());
        given(folderService.moveFolderToNewParent(any(Folder.class), any(Folder.class), anyInt())).willReturn(
                new ResponseEntity ("Alter Elternordner nicht gefunden oder Benutzer nicht berechtigt.", HttpStatus.FORBIDDEN)
        );

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("targetid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().string("Alter Elternordner nicht gefunden oder Benutzer nicht berechtigt."));
    }

    @Test
    public void folderController_MoveFolder_ReturnsInternalServerError() throws Exception {
        User user = new User();
        user.setId(1);

        Folder folder = new Folder();
        folder.setId(1);
        folder.setUserId(1);
        folder.setParentFolderId(2);

        Folder newParentFolder = new Folder();
        newParentFolder.setId(3);
        newParentFolder.setUserId(1);

        Folder oldParentFolder = new Folder();
        oldParentFolder.setFolders(new ArrayList<>());
        oldParentFolder.setId(2);
        oldParentFolder.setUserId(1);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(1)).willReturn(Optional.of(folder));
        given(folderRepo.findById(3)).willReturn(Optional.of(newParentFolder));
        given(folderRepo.findById(2)).willReturn(Optional.of(oldParentFolder));
        given(folderService.moveFolderToNewParent(any(Folder.class), any(Folder.class), anyInt())).willReturn(
                new ResponseEntity ("Fehler beim Speichern des Ordners.", HttpStatus.INTERNAL_SERVER_ERROR)
        );

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("targetid", "3"));

         response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(content().string("Fehler beim Speichern des Ordners."));
    }

    @Test
    public void folderController_MoveFolder_ReturnsOk() throws Exception {
        User user = new User();
        user.setId(1);

        Folder folder = new Folder();
        folder.setId(1);
        folder.setFolderName("folderName");
        folder.setUserId(1);
        folder.setParentFolderId(1);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any(User.class))).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));
        given(folderService.moveFolderToNewParent(any(Folder.class), any(Folder.class), anyInt())).willReturn(
                new ResponseEntity (HttpStatus.OK)
        );

        ResultActions response = mockMvc.perform(put("/testuser/movefolder")
                .header("Authorization", "validToken")
                .param("id", "1")
                .param("targetid", "3"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }



    @Test
    public void folderController_DeleteFolder_ReturnsUnauthorized() throws Exception {
        given(userRepo.findByUsername(anyString())).willReturn(Optional.empty());
        given(jwtService.isTokenValid(anyString(), any())).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(new Folder()));

        ResultActions response = mockMvc.perform(delete("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig."));
    }

    @Test
    public void folderController_DeleteFolder_ReturnsUnauthorized2() throws Exception{
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any())).willReturn(false);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(new Folder()));

        ResultActions response = mockMvc.perform(delete("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().string("Benutzer nicht gefunden oder Token ungültig."));
    }

    @Test
    public void folderController_DeleteFolder_ReturnsNotFound() throws Exception{
        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(new User()));
        given(jwtService.isTokenValid(anyString(), any())).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(delete("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Ordner nicht gefunden oder Benutzer nicht berechtigt."));
    }

    @Test
    public void folderController_DeleteFolder_ReturnsNotFound2() throws Exception{
        User user = new User();
        user.setId(1);

        Folder folder = new Folder();
        folder.setUserId(2);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any())).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));

        ResultActions response = mockMvc.perform(delete("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string("Ordner nicht gefunden oder Benutzer nicht berechtigt."));
    }

    @Test
    public void folderController_DeleteFolder_ReturnsForbidden() throws Exception{
        User user = new User();
        user.setId(1);

        Folder folder = new Folder();
        folder.setUserId(1);
        folder.setParentFolderId(0);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any())).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));

        ResultActions response = mockMvc.perform(delete("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().string("Dieser Ordner darf nicht gelöscht werden."));
    }

    @Test
    public void folderController_DeleteFolder_ReturnsForbidden2() throws Exception{
        User user = new User();
        user.setId(1);

        Folder folder = new Folder();
        folder.setUserId(1);
        folder.setParentFolderId(0);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any())).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));
        given(folderRepo.findById(0)).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(delete("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().string("Dieser Ordner darf nicht gelöscht werden."));
    }

    @Test
    public void folderController_DeleteFolder_ReturnsForbidden3() throws Exception{
        User user = new User();
        user.setId(1);

        Folder folder = new Folder();
        folder.setUserId(1);
        folder.setParentFolderId(0);

        Folder parentFolder = new Folder();
        parentFolder.setUserId(0);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any())).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));
        given(folderRepo.findById(0)).willReturn(Optional.of(parentFolder));

        ResultActions response = mockMvc.perform(delete("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().string("Dieser Ordner darf nicht gelöscht werden."));
    }

    @Test
    public void folderController_DeleteFolder_ReturnsInternalServerError() throws Exception{
        User user = new User();
        user.setId(1);

        Folder folder = new Folder();
        folder.setUserId(1);
        folder.setParentFolderId(1);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any())).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));
        given(folderService.removeFolder(any(Folder.class), any(Folder.class))).willReturn(
                new ResponseEntity ("Fehler beim Löschen des Ordners.", HttpStatus.INTERNAL_SERVER_ERROR)
        );

        ResultActions response = mockMvc.perform(delete("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(content().string("Fehler beim Löschen des Ordners."));
    }

    @Test
    public void folderController_DeleteFolder_ReturnsOk() throws Exception{
        User user = new User();
        user.setId(1);

        Folder folder = new Folder();
        folder.setUserId(1);
        folder.setParentFolderId(1);

        given(userRepo.findByUsername(anyString())).willReturn(Optional.of(user));
        given(jwtService.isTokenValid(anyString(), any())).willReturn(true);
        given(folderRepo.findById(anyInt())).willReturn(Optional.of(folder));
        given(folderService.removeFolder(any(Folder.class), any(Folder.class))).willReturn(
                new ResponseEntity (HttpStatus.OK)
        );

        ResultActions response = mockMvc.perform(delete("/testuser/folder")
                .header("Authorization", "validToken")
                .param("id", "1"));

        response.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
