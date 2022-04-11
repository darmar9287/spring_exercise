package com.spring.exercise.service;

import com.spring.exercise.model.UserModel;
import com.spring.exercise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

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
}
