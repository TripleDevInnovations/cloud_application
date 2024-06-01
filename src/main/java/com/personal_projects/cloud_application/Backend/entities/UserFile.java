package com.personal_projects.cloud_application.Backend.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.File;

@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class UserFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userFile_seq")
    @SequenceGenerator(name = "userFile_seq", sequenceName = "userFile_sequence", allocationSize = 1)
    private int id;
    private String fileName;
    private String fileType;
    private String size;
    private File file;

}
