package com.momo2x.study.springmvc.config;

import java.util.Arrays;
import java.util.function.Predicate;

public enum RoleType {

    USER,
    ADMIN;

    public String authority() {
        return "ROLE_" + this.name();
    }

    public static String authority(final RoleType role) {
        return "ROLE_" + role.name();
    }

    public static boolean isAuthority(final String auth) {
        final Predicate<RoleType> isRoleAuthority = role -> authority(role).equals(auth);

        return Arrays
                .stream(values())
                .anyMatch(isRoleAuthority);
    }

}
