package com.spring.exercise.utils;

import com.spring.exercise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueMailValidator implements ConstraintValidator<UniqueMail, String> {


    @Autowired
    UserService userService;

    @Override
    public void initialize(UniqueMail mail) {
    }

    @Override
    public boolean isValid(String mail, ConstraintValidatorContext cxt) {
        return !userService.checkIfUserExistsInDb(mail);
    }

}
