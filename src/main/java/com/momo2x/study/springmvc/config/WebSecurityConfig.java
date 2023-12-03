package com.momo2x.study.springmvc.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.momo2x.study.springmvc.config.RoleType.ADMIN;
import static com.momo2x.study.springmvc.config.RoleType.USER;
import static com.momo2x.study.springmvc.config.ViewPath.URL;
import static java.util.Optional.ofNullable;
import static org.springframework.security.core.userdetails.User.withDefaultPasswordEncoder;
import static org.springframework.security.web.WebAttributes.AUTHENTICATION_EXCEPTION;

@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(
                        requests -> requests
                                .requestMatchers(URL.USER.PATH +"**").hasRole(USER.name())
                                .requestMatchers(URL.ADMIN.PATH +"**").hasRole(USER.name())
                                .anyRequest().permitAll()
                )
                .formLogin()
                .successHandler(authenticationSuccessHandler())
                .and()
                .logout(logout -> logout.logoutSuccessUrl(URL.ROOT.PATH))
                .build();
    }

    @Bean
    @SuppressWarnings("deprecation")
    public InMemoryUserDetailsManager userDetailsService() {
        return new InMemoryUserDetailsManager(
                withDefaultPasswordEncoder()
                        .username("myuser")
                        .password("mypassword")
                        .roles(USER.name())
                        .build(),
                withDefaultPasswordEncoder()
                        .username("admin")
                        .password("adminpwd")
                        .roles(USER.name(), ADMIN.name())
                        .build()
        );
    }

    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            private record RoleData(int priority, String url) {}

            private static final Map<String, RoleData> ROLE_DATA_BY_AUTH = Map.of(
                    ADMIN.authority(), new RoleData(1, URL.ADMIN.ADMIN),
                    USER.authority(), new RoleData(2, URL.USER.MAIN)
            );

            @Override
            public void onAuthenticationSuccess(
                    final HttpServletRequest request,
                    final HttpServletResponse response,
                    final Authentication authentication)
                    throws IOException {
                if (response.isCommitted()) {
                    return;
                }

                new DefaultRedirectStrategy().sendRedirect(request, response, this.findTargetUrlByRole(authentication));
                this.clearAuthenticationException(request);
            }

            private String findTargetUrlByRole(final Authentication authentication) {
                final Predicate<GrantedAuthority> byValidRole = auth -> ROLE_DATA_BY_AUTH.containsKey(auth.getAuthority());
                final Function<GrantedAuthority, RoleData> toRoleData = auth -> ROLE_DATA_BY_AUTH.get(auth.getAuthority());
                final Comparator<RoleData> byPriority = Comparator.comparingInt(RoleData::priority);

                return authentication
                        .getAuthorities()
                        .stream()
                        .filter(byValidRole)
                        .map(toRoleData)
                        .sorted(byPriority)
                        .map(RoleData::url)
                        .findFirst()
                        .orElseThrow(IllegalArgumentException::new);
            }

            private void clearAuthenticationException(final HttpServletRequest request) {
                final Optional<HttpSession> requestSession = ofNullable(request.getSession(false));
                final Consumer<HttpSession> removeAuthExceptionFromSession =
                        session -> session.removeAttribute(AUTHENTICATION_EXCEPTION);

                requestSession.ifPresent(removeAuthExceptionFromSession);
            }
        };
    }

}
