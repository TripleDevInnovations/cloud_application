package com.personal_projects.cloud_application.backend.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.ToString;
import lombok.Data;

import java.util.List;

@Data
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "folder_seq")
    @SequenceGenerator(name = "folder_seq", sequenceName = "folder_sequence", allocationSize = 1)
    private int id;

    private String folderName;
    private String path;
    private int userId;
    private int parentFolderId;

    @OneToMany
    private List<Folder> folders;

    @OneToMany
    private List<UserFile> files;

}
