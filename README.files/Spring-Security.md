# Applying Spring Security to our pages

Initially I'll use a simple and "automagic" login based authentication feature provided by Spring Boot. The intention 
is to evolve this authorization mechanism to use OAuth2 and sign in using Google, Facebook, GitHub and so on.

#### Index
- [Dependencies](#dependencies)
  - [Using Spring Initializer](#using-spring-initializr)
  - [Main dependencies](#main-dependencies)
  - [Security dependencies](#security-dependencies)
- [Configuration](#configuration)
  - [Default Login](#default-login)
  - [Hard coding credentials](#hard-coding-credentials)
- [Redirecting after login](#redirecting-after-login)
- [Restricting access by role](#restricting-access-by-role)
- [Allowing anonymous access](#allowing-anonymous-access)
- [Logout redirection](#logout-redirection)
- [Last touch](#last-touch)
  - [Other secured and unsecured pages](#other-secured-and-unsecured-pages)

## Dependencies

### Using Spring Initializr

The basic configuration is the same as explained in [Working with Spring Boot MVC and JSP pages](./Spring-MVC-&-JSP.md),
so check it out to start configuring the project. In addition, some other dependencies must be included in order to 
make security work.

- Dependencies:
    - Spring Web (see notes for the remaining dependencies)

### Main dependencies

This dependency is an addition to the original ones at 
[Spring Boot MVC and JSP pages Main Dependencies](./Spring-MVC-&-JSP.md#main-dependencies).

### Security dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
## Configuration

### Default Login

By default, adding Spring Security as a dependency without any further configuration will force the application to 
render a default login page preventing the user accessing the desired resource without signing in. The login page will 
be at `host/login` and also a default logout page will be at `host/logout`. 

The default login credential is `user`, and the password varies everytime the application is started. Something like:

```
YYYY-MM-DDTHH:mm:SS.SSSSS  WARN NNNN --- [           main] .s.s.UserDetailsServiceAutoConfiguration :

Using generated security password: 6c989a9a-d0b0-439a-9ba8-c26b85698d0b

This generated password is for development use only. Your security configuration must be updated before running your application in production.
```

### Hard coding credentials

First I'll change the default user for a hard coded one stored in memory. Not for release, but least I won't have to 
copy and paste the password all the time. Also, I'll be able to add the user's role later to be able to redirect to 
different pages after the login and also to limit his access to other pages and endpoints.

```java
@Configuration
public class WebSecurityConfig {
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withDefaultPasswordEncoder()
                        .username("myuser")
                        .password("mypassword")
                        .build()
        );
    }
}
```

## Redirecting after login

Let's assume we have an admin user type besides the regular one. So I want that both are redirected to different pages 
after login.

First we need a second JSP to redirect to and map it to a new endpoint (without having to create a whole controller). I 
created a new page called `admin.jsp`.

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Spring MVC w/ JSP and Security</title>
</head>
<body>
    Admin Page
</body>
</html>
```

And mapped it to a different endpoint in my `WebConfig`
```java
...
return new WebMvcConfigurer() {
    ...
    public void addViewControllers(final ViewControllerRegistry registry) {
        ...
        registry.addViewController("/admin").setViewName("admin");
    }
};
```

Then, let's add a new hardcoded user, and set different roles for all. 

(!) NOTE: I'll be using static imports from now on. So many methods being called may seem to be from the class they are 
 from (e.g. `withDefaultPasswordEncoder`).

```java
@Configuration
public class WebSecurityConfig {
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        return new InMemoryUserDetailsManager(
                withDefaultPasswordEncoder()
                        .username("myuser")
                        .password("mypassword")
                        .roles("USER")
                        .build(),
                withDefaultPasswordEncoder()
                        .username("admin")
                        .password("adminpwd")
                        .roles("USER", "ADMIN")
                        .build()
        );
    }
}
```

Now let's create a "successful authentication handler". I could declare a separate class extending 
`AuthenticationSuccessHandler` and declare it as a `@Component` or even return a new instance of it in from a `@Bean` 
method inside our configuration class. But I want to keep it totally wrapped inside the same class.

First I have to define a better login handling adding a security filter in the filter chain in our security 
configuration. The code below use deprecated API, but when it's time to move forward to better security sign ins we 
won't need it anymore.

The code below is just an explicit way to do the same as the default authentication login page, but gives us more 
flexibility to define what happens if the user is successfully authenticated.

```java
@Configuration
public class WebSecurityConfig {
    ...
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
      return http
              .authorizeHttpRequests()
              .anyRequest()
              .authenticated()
              .and()
              .formLogin()
              .successHandler(authenticationSuccessHandler())
              .and()
              .build();
    }
    ...
}
```

More details regarding the code below can be found in this fine article from Baeldung named 
[Redirect to Different Pages After Login With Spring Security](https://www.baeldung.com/spring-redirect-after-login). 
You didn't think I came up with this whole code by myself, did you? (at most I refactored it a bit)

Basically, a new `AuthenticationSuccessHandler` instance must be created in order to be used by the success handler of 
the login form. A few validations here and there and the handler checks the roles belonging to the user and redirects 
to the mapped uri according tho the role found. In case the user has more than one role, it will consider the one with 
the highest priority.

```java
private AuthenticationSuccessHandler authenticationSuccessHandler() {
    return new AuthenticationSuccessHandler() {
        private record RoleData(int priority, String url) {}

        private static final Map<String, RoleData> ROLES = Map.of(
                "ROLE_ADMIN", new RoleData(1, "/admin"),
                "ROLE_USER", new RoleData(2, "/main")
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
            final Predicate<GrantedAuthority> byValidRole = auth -> ROLES.containsKey(auth.getAuthority());
            final Function<GrantedAuthority, RoleData> toRoleData =  auth -> ROLES.get(auth.getAuthority());
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
```

## Restricting access by role

The next step is to prevent a regular user to access the administration page, but the administrator must have access to 
both pages.

I'm changing the security filter chain to instead of using `.anyRequest()`, use the customizer lambda expression passed 
as argument, setting the request matchers and roles for each matcher. Since previously the admin credential was created 
with both roles, "USER" and "ADMIN", it will be able to access both resources.

```java
@Bean
public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(
                    requests -> requests
                            .requestMatchers("/main")
                            .hasRole("USER")
                            .requestMatchers("/admin")
                            .hasRole("ADMIN")
                            .anyRequest()
                            .authenticated()
            )
            .formLogin()
            .successHandler(authenticationSuccessHandler())
            .and()
            .build();
}
```

## Allowing anonymous access

Until now, we've been restricting all access to the application, meaning that every single page needs authentication. 
But what if I want to separate my resources into "secured" and "unsecured" resource? Like any anonymous request can 
access like a main open to anyone page?  

We need to remove the `.anyRequest().authenticated()` authorization from our security filter chain and replace it by
`.anyRequest().permitAll()`. Everything that hasn't been explicitly secured will be free of use. To remove the 
complexity of having to secure every single resource that needs authorization I'll create the following folder 
structure moving the current file accordingly:
```
.
└── WEB-INF
    └── view
        ├── header.jspf
        ├── index.jsp
        └── secured
            ├── admin
            │   └── admin.jsp
            └── user
                └── main.jsp
```

So, any page under `secured/user` will be accessible to normal users and admins will have access to any resource they 
want. Not logged in visitors, however, will only be able to see `index.jsp`.

Before all, I just can't keep using literal string everywhere. So I will create a set of constants to better address 
this issue being easier to refactor it later, in case I need it.

```java
public final class ViewPath {

    public static final class URL {
        public final static class ROOT {
            public static final String PATH = "/";
            public static final String OTHER = ROOT.PATH + "other";
        }

        public final static class USER {
            public static final String PATH = ROOT.PATH + "user/";
            public static final String MAIN = PATH + "main";
            public static final String OTHER = PATH + "other";
        }

        public final static class ADMIN {
            public static final String PATH = ROOT.PATH + "admin/";
            public static final String ADMIN = PATH + "admin";
            public static final String OTHER = PATH + "other";
        }
    }

}
```

So, if I need to address `/user` path, I just need to use `URL.USER.PATH` and if I need to address `/user/main` 
resource, I just need to use `URL.USER.MAIN`. In case those paths values change, I just need to change the constants.

Anyway... Authorize HTTP request in my security filter chain now will be...

```java
...
.authorizeHttpRequests(
        requests -> requests
                .requestMatchers(URL.USER.PATH +"**").hasRole("USER")
                .requestMatchers(URL.ADMIN.PATH +"**").hasRole("ADMIN")
                .anyRequest().permitAll()
)
...
```

That's the same as if I typed to `/user/**` and `/admin/**`. It means that anything under `/user` will be available 
for users with role `USER` and everything under `/admin` will be available for users with role `ADMIN`. The rest of the 
resources will be available to anyone.

Something similar must be done to the `WebConfig`, but also change the path for the JSPs.

```java
@Bean
public WebMvcConfigurer webMvcConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addViewControllers(@NonNull final ViewControllerRegistry registry) {
            registry.addViewController(ROOT).setViewName("index");
            registry.addViewController(USER.MAIN).setViewName("secured/user/main");
            registry.addViewController(ADMIN.ADMIN).setViewName("secured/admin/admin");
        }
    };
}
```

Now anyone can access `/`, but to access `/user/main` or `/admin/admin` it will require to log in.

## Logout redirection

After logout, the default behavior is to send the user back to the login page. I'd rather to be redirected to the main
anonymous page.

To do that it's actually quite simple (if you don't know different redirections according to the user logging out). Just
Add a `logout` method to the security filter chain as illustrated below:

```java
...
        
.and()
.logout(logout -> logout.logoutSuccessUrl(URL.ROOT.PATH))
.build();

...
```

## Last touch

Let's wrap up this security part with some last touches...

### Other secured and unsecured pages

I'll add new pages to better illustrate authorized and unauthorized access to resources.

Also, I'll change the pages a little bit. It will end up with something like this:

```
.
└── WEB-INF
    └── view
        ├── header.jspf
        ├── index.jsp
        ├── links.jspf
        ├── other.jsp
        └── secured
            ├── admin
            │   ├── admin.jsp
            │   └── other-admin.jsp
            └── user
                ├── main.jsp
                └── other-main.jsp
```

The new `links.jsp` page has a simple condition. If the user is authenticated, it will show a "Sign Out" link, if it
is not, the "Sign In" link.

Something like:
```html
<%@ page import="com.momo2x.study.springmvc.controller.ViewHelper" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<h3>
    <c:choose>
        <c:when test="${ViewHelper.authenticated}">
            To leave, please <a href="/logout">Sign out</a>.
        </c:when>
        <c:otherwise>
            Please <a href="/login">Sign In</a>.
        </c:otherwise>
    </c:choose>
</h3>
```

The `ViewHelper` class is our "hack" to get if the user is authenticated or not. Spring security will always consider a
user authenticated like "anonymous" when setting up our security strategy, so calling
`SecurityContextHolder.getContext().getAuthentication().isAuthentixated()` will always return true.

```java
public class ViewHelper {

    public static boolean isAuthenticated() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(RoleType::isAuthority);
    }
}
```

For the sake of readability and reuse I created a `RoleType` enumerator that will help me with all Role vs. Authority
"complexity". Also, will allow me to refactor some code removing literals here and there.

```java
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
```

And just a glimpse of the refactoring stuff:

```
...

.requestMatchers(URL.USER.PATH +"**").hasRole(USER.name())
.requestMatchers(URL.ADMIN.PATH +"**").hasRole(USER.name())

...

.password("mypassword")
.roles(USER.name())
.build(),

...

.password("adminpwd")
.roles(USER.name(), ADMIN.name())
.build()

...

private static final Map<String, RoleData> ROLE_DATA_BY_AUTH = Map.of(
        ADMIN.authority(), new RoleData(1, URL.ADMIN.ADMIN),
        USER.authority(), new RoleData(2, URL.USER.MAIN)
);

...
```