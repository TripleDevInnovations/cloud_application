package com.personal_projects.cloud_application.repositories;

import com.personal_projects.cloud_application.backend.entities.User;
import com.personal_projects.cloud_application.backend.repositories.UserRepo;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.runner.RunWith;

import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepoTest {

    @Autowired
    private UserRepo userRepo;

    @Test
    public void userRepo_SaveAll_ReturnSavedUser() {
        //Arrange
        User user = new User();
        user.setUsername("viemrB%_V#W$3f7=_-=0$U^IdoP[DQ");
        user.setPassword("viemrB%_V#W$3f7=_-=0$U^IdoP[DQ");
        //Act
        User savedUser = userRepo.save(user);

        //Assert
        Assertions.assertThat(savedUser).isNotNull();
        Assertions.assertThat(savedUser.getId()).isGreaterThan(0);
        Assertions.assertThat(savedUser.getUsername()).isEqualTo("viemrB%_V#W$3f7=_-=0$U^IdoP[DQ");
        Assertions.assertThat(savedUser.getPassword()).isEqualTo("viemrB%_V#W$3f7=_-=0$U^IdoP[DQ");
    }

    @Test
    public void userRepo_GetAll_ReturnMoreThanOneUser() {
        //Arrange
        User user = new User();
        user.setUsername("viemrB%_V#W$3f7=_-=0$U^IdoP[DQ");
        User user2 = new User();
        user.setUsername("G3kd4Po0g_[a%jc_=ow]'PNd}qRnwb");

        //Act
        userRepo.save(user);
        userRepo.save(user2);

        List<User> users = userRepo.findAll();

        //Assert
        Assertions.assertThat(users).isNotNull();
        Assertions.assertThat(userRepo.findByUsername(user.getUsername())).isPresent();
        Assertions.assertThat(userRepo.findByUsername(user2.getUsername())).isPresent();
    }

    @Test
    public void userRepo_FindById_ReturnUserNotNull() {
        //Arrange
        User user = new User();
        user.setUsername("viemrB%_V#W$3f7=_-=0$U^IdoP[DQ");
        User user2 = new User();
        user2.setUsername("G3kd4Po0g_[a%jc_=ow]'PNd}qRnwb");

        //Act
        userRepo.save(user);
        userRepo.save(user2);

        User savedUser = userRepo.findById(user.getId()).get();
        User savedUser2 = userRepo.findById(user2.getId()).get();

        //Assert
        Assertions.assertThat(savedUser).isNotNull();
        Assertions.assertThat(savedUser.getUsername()).isEqualTo("viemrB%_V#W$3f7=_-=0$U^IdoP[DQ");
        Assertions.assertThat(savedUser2).isNotNull();
        Assertions.assertThat(savedUser2.getUsername()).isEqualTo("G3kd4Po0g_[a%jc_=ow]'PNd}qRnwb");
    }

    @Test
    public void UserRepo_UpdateUser_ReturnUserNotNull() {
        //Arrange
        User user = new User();
        user.setUsername("viemrB%_V#W$3f7=_-=0$U^IdoP[DQ");

        //Act
        userRepo.save(user);

        User savedUser = userRepo.findById(user.getId()).get();
        savedUser.setUsername("G3kd4Po0g_[a%jc_=ow]'PNd}qRnwb");

        User updateUser = userRepo.save(savedUser);

        //Assert
        Assertions.assertThat(updateUser.getUsername()).isEqualTo("G3kd4Po0g_[a%jc_=ow]'PNd}qRnwb");
        Assertions.assertThat(updateUser.getId()).isNotEqualTo(0);
    }

    @Test
    public void userRepo_DeleteUser_ReturnUserNotNull() {
        //Arrange
        User user = new User();
        user.setUsername("viemrB%_V#W$3f7=_-=0$U^IdoP[DQ");

        //Act
        userRepo.save(user);
        userRepo.deleteById(user.getId());
        Optional<User> userReturn = userRepo.findById(user.getId());

        //Assert
        Assertions.assertThat(userReturn).isEmpty();
    }
}
