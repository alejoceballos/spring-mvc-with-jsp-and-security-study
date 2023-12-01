# Creating a Spring Web MVC Application with JSPs

Yes, I'm not using Thymeleaf. I'm trying to render very simple pages, using very simple stuff so I can focus on the 
back-end. For the time I advance for modern front-end technologies I'll use some SPA (e.g. React) instead of server-side 
rendering engines.

#### Index
- [Using Spring Initializer](#using-spring-initializr)
- [Notes](#notes)
  - [Packaging WAR](#packaging)
  - [Main dependencies](#main-dependencies)
- [JavaServer Pages](#javaserver-pages)
  - [JSP location](#jsp-location)
  - [Application Properties](#application-properties)
  - [Redirecting Requests to the JSP](#redirecting-requests-to-the-jsp)
- [JavaServer Pages Fragments](#javaserver-pages-fragments)
- [JavaServer Pages Standard Tag Library](#javaserver-pages-standard-tag-library)
  - [JSTL Dependencies](#jstl-dependencies)
  - [Using a core TagLib](#using-a-core-taglib)
  - [Notes about jsp:include](#notes-about-jspinclude)

## Using Spring Initializr

- Go to: [Spring Initialzr](https://start.spring.io/) page
- Select the following main options:
    - Project: Maven (yes, I still use maven)
    - Language: Java (yes, no Kotling or Groovy)
    - Spring Boot: 3.2.0 (at the time of this writing, that was the latest non snapshot version)
    - Packaging: war (see notes below)
    - Java: 21 (at the time of this writing, this was the latest LTS version)
    - Dependencies:
        - Spring Web (see notes for the remaining dependencies)

## Notes

### Packaging

I've been looking across the web and saw several comments regarding being unable to render JSP pages using packaging 
JAR file in pom. I tried both at early stages of the development and running the application using IntelliJ showed no 
difference. Anyway, in order to prevent all possible trouble in the near future I'm packaging it as a WAR file and will 
try packaging as JAR later.

In `pom.xml` use:

```xml        I
<packaging>war</packaging>
```

### Main dependencies

#### Web MVC:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

#### To work with JavaServer Pages:

```xml
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-jasper</artifactId>
    <version>10.1.16</version>
</dependency>
```

(!) NOTE: at the time of this writing, this was the latest version.

Not adding this dependency will imply in:
```
Whitelabel Error Page

This application has no explicit mapping for /error, so you are seeing this as a fallback.
...
There was an unexpected error (type=Not Found, status=404).
```

## JavaServer Pages

### JSP location

Create the `webapp/WEB-INF/view` directory under `src/main`.

### Application Properties

Create an `application.yml` file under `src/main/resources`.

NOTE: Using `.yml` instead of `.properties` due to its organization nature.

Add the following configuration to YML file:

```yml
spring:
    mvc:
      view:
        prefix: /WEB-INF/view/
        suffix: .jsp
``` 

I created a simple JSP page called `main.jsp` to be rendered in the browser:

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Spring MVC w/ JSP and Security</title>
</head>
<body>
    Main Page
</body>
</html>
```

### Redirecting Requests to the JSP

Normally, when requesting for a page a `@Controller` class must be created with a `@GetMapping` method that returns a 
string with the resource name to be served, like `page.jsp` without the `.jsp` part. It is useful for server-side 
rendered pages when you need to perform some processing before loading the page and also to be able pass data to be 
displayed during rendering.

But when I do not need any dynamic data rendered on the page, a simple configuration may be used:

```java
@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(final ViewControllerRegistry registry) {
                registry.addViewController("/").setViewName("main");
            }
        };
    }

}
```

(!) NOTE: `WebConfig` could have extended from `WebMvcConfigurer` and implemented `addViewControllers`. It has the same
result.

## JavaServer Pages Fragments

JSPF is one of the strategies to allow reusing code in multiples pages. I created a JSPF file for the page header and
reused it in both pages.

##### Creating the header.jsf

```html
<head>
  <title>Spring MVC w/ JSP and Security</title>
</head>
```

Note, there is no need to add the `%@ page` directive here since the JSPF will be parsed after being included in the 
JSP.

##### Changing main.jsp & admin.jsp

```html
<html>
<%@include file="header.jspf" %>
<body>
    ...
</body>
</html>
```

## JavaServer Pages Standard Tag Library

### JSTL Dependencies

First I have to underline our Java and JEE versions. Since they are the newest (by the time of this writing), old JSTL
implementations may be sometimes problematic, try using the newest from Jakarta.

```xml
<dependency>
    <groupId>jakarta.servlet.jsp.jstl</groupId>
    <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
    <version>3.0.0</version>
</dependency>

<dependency>
    <groupId>org.glassfish.web</groupId>
    <artifactId>jakarta.servlet.jsp.jstl</artifactId>
    <version>3.0.1</version>
</dependency>
```

### Using a core TagLib

Updating the `header.jspf` fragment.

```html
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<head>
    <title>
        <c:out value="Spring MVC w/ JSP and Security" />
    </title>
</head>
```

### Notes about jsp:include

By default, it is not possible to include JSPF files into JSPs using `jsp:include` tag. JSPF are supposed to be compiled 
along the JSP, and `jsp:include` first compiles the target to then include it in the main page. Some hacks include 
making `.jspf` extensions compilable as a JSP file by adding a new `jsp-config` in the `web.xml`, but it makes not a lot 
of sense to me.