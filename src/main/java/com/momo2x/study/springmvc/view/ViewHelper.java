package com.momo2x.study.springmvc.view;

import com.momo2x.study.springmvc.config.RoleType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class ViewHelper {

    public static boolean authenticated() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(RoleType::isAuthority);
    }
}
