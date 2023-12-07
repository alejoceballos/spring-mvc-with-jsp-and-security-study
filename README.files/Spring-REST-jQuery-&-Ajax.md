# RESTful API, jQuery & Ajax

The idea now is to mix an existing Web Application having multiple JSP resources and security already configured to a
REST api, also secured. A page must send a simple Ajax request and grab the results and our simple proof of concept
will be over, for now.

#### Index
- [The REST API](#the-rest-api)
- [Securing the REST API](#securing-the-rest-api)
- [Grabbing real users](#grabbing-the-real-users)
  - [Creating our model](#creating-our-model)
  - [Creating our service](#creating-our-service)
  - [Updating the controller](#updating-the-controller)
- [Adding client dynamic code](#adding-client-dynamic-code)
  - [jQuery Maven dependency](#jquery-maven-dependency)
  - [Importing jQuery in the client](#importing-jquery-in-the-client)
  - [The asynchronous Ajax call](#the-asynchronous-ajax-call)
  - [The caller](#the-caller)
- [API live documentation](#api-live-documentation)
  - [Swagger Maven dependency](#swagger-maven-dependency)
  - [Using the "Try it out" feature](#using-the-try-it-out-feature)
  - [Enhancing the API documentation](#enhancing-the-api-documentation)

## The REST API

Basically I'll create an API (`/api/users`), accessible only by admins, that grabs all users and its authorities to be 
displayed in a list on the front-end. For now, it only returns mocked information in order to ger everything ready for 
what we want.

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping()
    public String list() {
        return """
                [
                    {
                        "username": "admin",
                        "authorities": "ROLE_ADMIN, ROLE_USER"
                    },
                    {
                        "username": "myuser",
                        "authorities": "ROLE_USER"
                    }
                ]
                """;
    }

}
```

## Securing the REST API

But after all the security configuration set, it will be available also for anonymous visitors. To close our API for 
everyone, but admins, add a matcher to the security filter:

```java
.requestMatchers("/api/**").hasRole(ADMIN.name())
```

## Grabbing the real users

It will be necessary to inject the `InMemoryUserDetailsManager` and hack its private `users` attribute. Also map the
current `UserDetails` to a simpler structure.

Firs, let´s create the model to be mapped from `UserDetails`:

### Creating our model

```java
public record UserData(String username, String authorities) {}
```

The `UserData` record holds the same structure than the one previously mocked.

### Creating our service

Now let's create the service that will inject the `InMemoryUserDetailsManager`.

```java
@Service
public class UserService {

    @Autowired
    private InMemoryUserDetailsManager userManager;
    
}
```

And add a helper method that will allow us to grab the data from its private `users` attribute trough reflection.

```java
private Map<String, UserDetails> getUsersFromUserManager() {
    try {
        final var field = InMemoryUserDetailsManager.class.getDeclaredField("users");
        field.setAccessible(true);
        return (Map<String, UserDetails>) field.get(userManager);
    } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new RuntimeException(e);
    }
}
```

And add another one that maps from `UserDetail` to `UserData`. Note that the `authorities` attribute from `UserDetails`
is a collection of `GrantedAuthority` objects, so it must be transformed to a list of strings with the authority names:

```java
private UserData toUserData(final UserDetails userDetails) {
    return new UserData(
            userDetails.getUsername(),
            userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(",")));
}
```

Now we use both "utility" methods in the main one. 

```java
public List<UserData> findUsers() {
    final var usersDetails = this.getUsersFromUserManager();

    return usersDetails.values()
            .stream()
            .map(this::toUserData)
            .collect(Collectors.toList());
}
```

### Updating the controller

Call the service from the controller:

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    public final UserService service;

    @GetMapping()
    public List<UserData> list() {
        return service.findUsers();
    }

}
```

(!) NOTE: Note Lombok usage here with `@RequiredArgsConstructor` so I can keep good programming practices (constructor 
dependency injection) avoiding the `@Autowired` annotation and no need to boilerplate the code with a constructor.

## Adding client dynamic code

Since I'm going old school here, to dynamically use Ajax in the front-end jQuery is my preferable choice, but also, 
since I'm going totally Java freak here, I will use a maven dependency to be able to work with jQuery!

### jQuery Maven dependency

- References:
  - https://www.webjars.org/
  - https://www.webjars.org/documentation#springboot
- Maven:
  - https://mvnrepository.com/artifact/org.webjars/webjars-locator-core
  - https://mvnrepository.com/artifact/org.webjars/jquery

```xml
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>webjars-locator-core</artifactId>
    <version>0.55</version>
</dependency>

<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>jquery</artifactId>
    <version>3.7.1</version>
</dependency>
```

`webjars-locator-core` must be used in case I don't want to specify the exact version o jQuery being used (and I don't).

### Importing jQuery in the client

Let's do it only once. Use it in the `header.jspf`.

```html
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<head>
    <script src="/webjars/jquery/jquery.min.js"></script>
    ...
</head>
```

### The asynchronous Ajax call

I won't take too long on this topic since, right now, front-end is not really my focus, only the API access from a 
browser using asynchronous calls.

I'll create a simple `api-client.js` file to make the Ajax call. I'll put it the `static` folder under `main/resources`.
It is going to be public to anyone and I won't restrict its access.

```javascript
const getUsers = (onSuccess, onError) =>
    $.ajax({
        url: "/api/users",
        type: "get",
        success: response => onSuccess(response),
        error: xhr => onError(xhr)
    });
```

Also add it to the `header.jspf`:

```html
<%@ page import="com.momo2x.study.springmvc.view.ViewHelper" %>
...
<head>
    ...
    <c:if test="${ViewHelper.authenticated()}">
        <script src="/api-client.js"></script>
    </c:if>
    ...
</head>
```

### The caller

I'm going to put a very simple, ugly table in the admin page. It will list all users.

```html
...
<body>
...
<table>
    <thead>
    <tr>
        <th>USER</th>
        <th>AUTHORIZATIONS</th>
    </tr>
    </thead>
    <tbody id="tbl">
    </tbody>
</table>
</body>
...
```

And dynamically update it with this JavaScript/jQuery code:

```javascript
const displayUsers = () => {
    const tBody = $("#tbl");

    const onSuccess = response =>
        response.forEach(obj => tBody.append(
            `<tr>
                <td>${obj.username}</td>
                <td>${obj.authorities}</td>
            </tr>`)
        );

    const onError = () =>
        tBody.append(
            `<tr>
                <td colspan="2">ERROR</td>
            </tr>`
        );

    getUsers(onSuccess, onError);
};
```

I know, it's hardcoded, string literal interpolation, no character escaped and blah blah blah. Not the point right now.
My goal, right now, is to test my API from the client code. I'll use a professional front-end framework later (perhaps).

I saved this code to a file name `view-admin.js` side-by-side the previous one because I'm feeling lazy right now to 
think about a nice vanilla JavaScript modular architecture. I'm kindda in a hurry for the next steps.

So the only thing left for this to work is call `displayUsers()` in `admin.js`:

```html
...
<html>
...
<body>
...
</body>
<script>
    displayUsers();
</script>
</html>
```

## API live documentation

- References: 
  - https://swagger.io/tools/open-source/
  - https://medium.com/@f.s.a.kuzman/using-swagger-3-in-spring-boot-3-c11a483ea6dc
  - https://www.baeldung.com/spring-rest-openapi-documentation
- Maven: 
  - https://mvnrepository.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-ui/

Let's create a nice API documentation to help developers.

### Swagger Maven dependency

Adding...

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

... Is enough to show all endpoints of our API, and since it is out of our secured area, anyone can see it, but not
use it, since the user must be authenticated as an administrator to perform a request to it.

### Using the "Try it out" feature

In order to be able to "Try it out", meaning to use the Swagger Open API screen to send a real request to the server an
admin must be authenticated. Add the following to the authorize HTTP requests.

```java
...
.requestMatchers("/swagger-ui/**").hasRole(ADMIN.name())
...
```

### Enhancing the API documentation

And just to have a more detailed and personalized information regarding the API, add `@Tag`, `@Operation` and 
`@ApiResponse` annotations to the Controller.

```java
@Tag(name = "User", description = "Operations regarding users")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    public final UserService service;

    @Operation(summary = "List all users and its authorities.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns all users",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserData.class))})})
    @GetMapping()
    public List<UserData> list() {
        return service.findUsers();
    }

}
```