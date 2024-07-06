package com.personal_projects.cloud_application.backend.entities;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Data;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import java.io.Serial;


@Data
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_sequence", allocationSize = 1)
    private int id;

    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne
    private Folder rootFolder;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
