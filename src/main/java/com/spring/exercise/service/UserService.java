package com.spring.exercise.service;

import com.spring.exercise.exceptions.CustomExceptions;
import com.spring.exercise.exceptions.PasswordException;
import com.spring.exercise.exceptions.UserAlreadyExists;
import com.spring.exercise.model.UserModel;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.utils.AppStringContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) {
        UserModel foundByEmail = userRepository.findByMail(email);
        String mail = foundByEmail.getMail();
        String password = foundByEmail.getPassword();
        return new User(mail, password, new ArrayList<>());
    }

    public UserModel createUser(String mail, String password) {
        UserModel userModel = new UserModel(mail, password);
        userModel.setPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(userModel);
        return userModel;
    }

    public void validateUserCredentials(String mail, String password, Errors errors) {
        if (!checkIfPasswordMatchRequirements(password)) {
            throw new PasswordException(errors);
        }
        if (checkIfUserExistsInDb(mail)) {
            throw new UserAlreadyExists(errors);
        }
    }

    public boolean checkIfUserExistsInDb(String mail) {
        return (userRepository.findByMail(mail) != null) ? true : false;
    }

    private boolean checkIfPasswordMatchRequirements(String pass) {
        return pass.length() >= 4 && pass.length() <= 20;
    }
}
