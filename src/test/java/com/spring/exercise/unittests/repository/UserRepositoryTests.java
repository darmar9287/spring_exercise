package com.spring.exercise.unittests.repository;

import com.spring.exercise.model.UserEntity;
import com.spring.exercise.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@DataMongoTest
@RunWith(SpringRunner.class)
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    private UserEntity user;

    private final static String USER_NAME = "marek_test@gmail.com";
    private final static String USER_PASS = "pass";

    @BeforeEach
    public void dataSetup() {
        user = new UserEntity();
        user.setId(ObjectId.get().toString());
        user.setUserName(USER_NAME);
        user.setPassword(USER_PASS);
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }


    @Test
    public void shouldSaveUserIfUsernameDoesNotExists() {
        //when
        UserEntity savedUser = userRepository.save(user);
        //then
        Optional<UserEntity> fetchedUser = userRepository.findByUserName(savedUser.getUserName());
        assertEquals(savedUser.getUserName(), fetchedUser.get().getUserName());
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