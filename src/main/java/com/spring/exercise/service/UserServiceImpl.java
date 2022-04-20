package com.spring.exercise.service;

import com.spring.exercise.exceptions.MailWrongFormatException;
import com.spring.exercise.exceptions.PasswordException;
import com.spring.exercise.exceptions.UserAlreadyExists;
import com.spring.exercise.model.UserModel;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String userName) {
        UserModel user = userRepository.findByUserName(userName);
        return UserDetailsImpl.build(user);
    }

    public UserModel createUser(UserModel user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return user;
    }

    public void validateUserCredentials(boolean shouldVerifyUserExists, String username, String password, Errors errors) {
        if (!checkIfPasswordMatchRequirements(password)) {
            throw new PasswordException(errors);
        }
        if(shouldVerifyUserExists && checkIfUserExistsInDb(username)) {
            throw new UserAlreadyExists(errors);
        }
        if(!checkIfMailIsWellFormed(username)) {
            throw new MailWrongFormatException(errors);
        }
    }

    private boolean checkIfMailIsWellFormed(String mail) {
        String patternString =  "^(.+)@(\\S+)$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(mail);
        return matcher.matches();
    }

    public boolean checkIfUserExistsInDb(String username) {
        return userRepository.findByUserName(username) != null;
    }

    private boolean checkIfPasswordMatchRequirements(String pass) {
        return pass.length() >=4 && pass.length() <= 20;
    }
}
