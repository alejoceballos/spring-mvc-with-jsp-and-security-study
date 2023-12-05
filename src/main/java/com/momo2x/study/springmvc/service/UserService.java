package com.momo2x.study.springmvc.service;

import com.momo2x.study.springmvc.model.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private InMemoryUserDetailsManager userManager;

    public List<UserData> findUsers() {
        final var usersDetails = this.getUsersFromUserManager();

        return usersDetails.values()
                .stream()
                .map(this::toUserData)
                .collect(Collectors.toList());
    }

    private Map<String, UserDetails> getUsersFromUserManager() {
        try {
            final var field = InMemoryUserDetailsManager.class.getDeclaredField("users");
            field.setAccessible(true);
            return (Map<String, UserDetails>) field.get(userManager);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private UserData toUserData(final UserDetails userDetails) {
        return new UserData(
                userDetails.getUsername(),
                userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")));
    }

}
