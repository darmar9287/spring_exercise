package com.spring.exercise.service;

import com.spring.exercise.model.UserModel;
import com.spring.exercise.repository.UserRepository;
import com.spring.exercise.utils.AppStringContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserModel foundByEmail = userRepository.findByEmail(email);
        if (foundByEmail == null) {
            return null;
        }
        String mail = foundByEmail.getEmail();
        String password = foundByEmail.getPassword();

        return new User(mail, password, new ArrayList<>());
    }

    public UserModel createUser(String mail, String password) {

        UserModel userModel = new UserModel(mail, password);

            userRepository.save(userModel);

        return userModel;
    }

    public void validateUserCredentials(String mail, String password, Map<String, String> errorMessages) {
        if (checkIfUserExistsInDb(mail)) {
            errorMessages.put("email", AppStringContainer.EMAIL_EXISTS_ERROR);
        }
        if (!checkIfPasswordMatchRequirements(password)) {
            errorMessages.put("password", AppStringContainer.PASSWORD_SIZE_ERROR);
        }
    }

    private boolean checkIfUserExistsInDb(String mail) {
        return (userRepository.findByEmail(mail) != null) ? true : false;
    }

    private boolean checkIfPasswordMatchRequirements(String pass) {
        return pass.length() >=4 && pass.length() <= 20;
    }
}
