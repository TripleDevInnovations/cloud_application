package com.personal_projects.cloud_application.Backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_sequence", allocationSize = 1)
    private int id;
    private String username;
    private String password;
    @OneToMany(cascade = CascadeType.ALL)
    private List<Folder> folders;
    @OneToMany(cascade = CascadeType.ALL)
    private List<UserFile> files;


}
