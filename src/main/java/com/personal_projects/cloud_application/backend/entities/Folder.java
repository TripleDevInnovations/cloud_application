package com.personal_projects.cloud_application.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @OneToMany(cascade = CascadeType.ALL)
    private List<Folder> folders;
    @OneToMany(cascade = CascadeType.ALL)
    private List<UserFile> files;
}
