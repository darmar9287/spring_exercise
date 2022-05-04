package com.spring.exercise.unittests.repository;

import com.spring.exercise.model.UserEntity;
import com.spring.exercise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertTrue;


@DataMongoTest
@RunWith(SpringRunner.class)
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    private UserEntity user;


    @BeforeEach
    public void dataSetup() {
        user = new UserEntity();
        user.setUserName("marek_test@gmail.com");
        user.setId("1");
        user.setPassword("pass");
    }


    @Test
    public void shouldSaveUserIfUsernameDoesNotExists() {
        //when
        UserEntity savedUser = userRepository.save(user);
        //then
        Optional<UserEntity> fetchedUser = userRepository.findByUserName(savedUser.getUserName());
        assertTrue(savedUser.getUserName().equals(fetchedUser.get().getUserName()));
    }

    @Test
    public void shouldReturnUserWhenUserFound() {
        //when
        UserEntity savedUser = userRepository.save(user);
        //then
        Optional<UserEntity> fetchedUser = userRepository.findByUserName(savedUser.getUserName());
        assertTrue(fetchedUser.isPresent());
    }

    @Test
    public void shouldNotReturnUserWhenUsernameNotFound() {
        //then
        Optional<UserEntity> fetchedUser = userRepository.findByUserName(user.getUserName());
        assertTrue(fetchedUser.isEmpty());
    }


}