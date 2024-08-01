package com.personal_projects.cloud_application.backend.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.ToString;
import lombok.Data;

@Data
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userFile_seq")
    @SequenceGenerator(name = "userFile_seq", sequenceName = "userFile_sequence", allocationSize = 1)
    private int id;

    private String fileName;
    private String fileType;
    private Long size;
    private int userId;
    private int folderId;

}
