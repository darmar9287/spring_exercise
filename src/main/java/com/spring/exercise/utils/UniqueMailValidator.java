package com.spring.exercise.utils;

import com.spring.exercise.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueMailValidator implements ConstraintValidator<UniqueMail, String> {
    
    @Autowired
    UserServiceImpl userServiceImpl;

    @Override
    public void initialize(UniqueMail mail) {
    }

    @Override
    public boolean isValid(String mail, ConstraintValidatorContext cxt) {
        return !userServiceImpl.checkIfUserExistsInDb(mail);
    }

}
