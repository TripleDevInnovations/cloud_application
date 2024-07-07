package com.personal_projects.cloud_application.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    private String path;
    private String user;
}
