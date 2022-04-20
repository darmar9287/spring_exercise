package com.spring.exercise.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spring.exercise.model.UserModel;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Setter
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private ObjectId id;

    private String userName;

    @JsonIgnore
    private String password;

    public UserDetailsImpl(ObjectId id, String userName, String password) {
        this.id = id;
        this.userName = userName;
        this.password = password;
    }

    public static UserDetailsImpl build(UserModel user) {

        return new UserDetailsImpl(
                user.getId(),
                user.getUserName(),
                user.getPassword());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
