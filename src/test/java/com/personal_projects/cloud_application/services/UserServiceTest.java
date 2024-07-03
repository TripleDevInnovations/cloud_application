package com.personal_projects.cloud_application.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.personal_projects.cloud_application.backend.entities.User;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import com.personal_projects.cloud_application.backend.services.impl.UserServiceImpl;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Arrange
        String username = "testUser";
        User mockUser = new User(); // Erstelle ein Mock-User-Objekt
        mockUser.setUsername(username);
        mockUser.setPassword("password");
        // Setze weitere notwendige Eigenschaften von User

        when(userRepo.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // Act
        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(username);

        // Assert
        Assertions.assertThat(userDetails).isNotNull();
        Assertions.assertThat(username).isEqualTo(userDetails.getUsername());
    }

    @Test
    public void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        String username = "nonExistentUser";
        when(userRepo.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> userService.userDetailsService().loadUserByUsername(username),
                        "Expected loadUserByUsername to throw, but it didn't");

        Assertions.assertThat(thrown.getMessage().contains("User not found")).isTrue();
    }
}
