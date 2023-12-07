# Simple Web Application with Simple Security
#### A study on how to create a Spring MVC project using old stuff (JSP, jQuery, Ajax), REST and Security.

The main idea of this proof of Concept project is to create an "Old School" Java Web Application using the newest Spring 
Boot version and its security features. With "Old School" I mean JSP server-side rendered web pages, no Thymeleaf (like
that's new school) and no Single Page Applications (SPA) (e.g. React). But I would like to use some asynchronous REST 
calls using old school Ajax with jQuery.

Initially, it starts as a single project wrapping all front-end, back-end, security, etc. to evolve to a more 
distributed architecture.

Initially our project structure will be like the following:

```
.
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── momo2x
    │   │           └── study
    │   │               └── springmvc
    │   │                   ├── config
    │   │                   ├── controller
    │   │                   ├── model
    │   │                   └── service
    │   ├── resources
    │   │   ├── application.yml
    │   │   ├── static
    │   │   └── templates
    │   └── webapp
    │       └── WEB-INF
    │           └── view
    │               ├── index.jsp
    │               └── secured
    └── test
```

To become something like:

```
.
├── pom.xml
├── front-end
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── com
│       │   │       └── momo2x
│       │   │           └── study
│       │   │               └── springmvc
│       │   │                   └── config
│       │   ├── resources
│       │   │   ├── application.yml
│       │   │   ├── static
│       │   │   └── templates
│       │   └── webapp
│       │       └── WEB-INF
│       │           └── view
│       │               ├── index.jsp
│       │               └── secured
│       └── test
│
└── back-end
    ├── pom.xml
    └── src
        ├── main
        │   ├── java
        │   │   └── com
        │   │       └── momo2x
        │   │           └── study
        │   │               └── springmvc
        │   │                   ├── config
        │   │                   ├── controller
        │   │                   ├── model
        │   │                   └── service
        │   └── resources
        │       └── application.yml
        └── test
```

And then will be split in even more different modules for the sake of concern segregation.

## Notes on Third-Party libraries

I'll be using, as long as it's possible, third-party dependencies in order to minimize the amount of code and/or for 
readability's sake.

### Lombok

> Project Lombok is a java library that automatically plugs into your editor and build tools, spicing up your java. 
> Never write another getter or equals method again, with one annotation your class has a fully featured builder, 
> Automate your logging variables, and much more.

- Reference: https://projectlombok.org/ 
- Maven: https://mvnrepository.com/artifact/org.projectlombok/lombok -->

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <scope>provided</scope>
</dependency>
```

## Creating the Application

#### Index:

1. [Creating a Spring Web MVC Application with JSPs](./README.files/Spring-MVC-&-JSP.md)
   - [Using Spring Initializer](./README.files/Spring-MVC-&-JSP.md#using-spring-initializr)
   - [Notes](./README.files/Spring-MVC-&-JSP.md#notes)
       - [Packaging WAR](./README.files/Spring-MVC-&-JSP.md#packaging)
       - [Main dependencies](./README.files/Spring-MVC-&-JSP.md#main-dependencies)
   - [JavaServer Pages](./README.files/Spring-MVC-&-JSP.md#javaserver-pages)
       - [JSP location](./README.files/Spring-MVC-&-JSP.md#jsp-location)
       - [Application Properties](./README.files/Spring-MVC-&-JSP.md#application-properties)
       - [Redirecting Requests to the JSP](./README.files/Spring-MVC-&-JSP.md#redirecting-requests-to-the-jsp)
   - [JavaServer Pages Fragments](./README.files/Spring-MVC-&-JSP.md#javaserver-pages-fragments)
   - [JavaServer Pages Standard Tag Library](./README.files/Spring-MVC-&-JSP.md#javaserver-pages-standard-tag-library)
       - [JSTL Dependencies](./README.files/Spring-MVC-&-JSP.md#jstl-dependencies)
       - [Using a core TagLib](./README.files/Spring-MVC-&-JSP.md#using-a-core-taglib)
       - [Notes about jsp:include](./README.files/Spring-MVC-&-JSP.md#notes-about-jspinclude)
2. [Applying Spring Security to our pages](./README.files/Spring-Security.md)
   - [Dependencies](./README.files/Spring-Security.md#dependencies)
       - [Using Spring Initializer](./README.files/Spring-Security.md#using-spring-initializr)
       - [Main dependencies](./README.files/Spring-Security.md#main-dependencies)
       - [Security dependencies](./README.files/Spring-Security.md#security-dependencies)
   - [Configuration](./README.files/Spring-Security.md#configuration)
       - [Default Login](./README.files/Spring-Security.md#default-login)
       - [Hard coding credentials](./README.files/Spring-Security.md#hard-coding-credentials)
   - [Redirecting after login](./README.files/Spring-Security.md#redirecting-after-login)
   - [Restricting access by role](./README.files/Spring-Security.md#restricting-access-by-role)
   - [Allowing anonymous access](./README.files/Spring-Security.md#allowing-anonymous-access)
   - [Logout redirection](./README.files/Spring-Security.md#logout-redirection)
   - [Last touch](./README.files/Spring-Security.md#last-touch)
       - [Other secured and unsecured pages](./README.files/Spring-Security.md#other-secured-and-unsecured-pages)
3. [RESTful API, jQuery & Ajax](./README.files/Spring-REST-jQuery-&-Ajax.md)
   - [The REST API](./README.files/Spring-REST-jQuery-&-Ajax.md#the-rest-api)
   - [Securing the REST API](./README.files/Spring-REST-jQuery-&-Ajax.md#securing-the-rest-api)
   - [Grabbing real users](./README.files/Spring-REST-jQuery-&-Ajax.md#grabbing-the-real-users)
       - [Creating our model](./README.files/Spring-REST-jQuery-&-Ajax.md#creating-our-model)
       - [Creating our service](./README.files/Spring-REST-jQuery-&-Ajax.md#creating-our-service)
       - [Updating the controller](./README.files/Spring-REST-jQuery-&-Ajax.md#updating-the-controller)
   - [Adding client dynamic code](./README.files/Spring-REST-jQuery-&-Ajax.md#adding-client-dynamic-code)
       - [jQuery Maven dependency](./README.files/Spring-REST-jQuery-&-Ajax.md#jquery-maven-dependency)
       - [Importing jQuery in the client](./README.files/Spring-REST-jQuery-&-Ajax.md#importing-jquery-in-the-client)
       - [The asynchronous Ajax call](./README.files/Spring-REST-jQuery-&-Ajax.md#the-asynchronous-ajax-call)
       - [The caller](./README.files/Spring-REST-jQuery-&-Ajax.md#the-caller)
   - [API live documentation](./README.files/Spring-REST-jQuery-&-Ajax.md#api-live-documentation)
       - [Swagger Maven dependency](./README.files/Spring-REST-jQuery-&-Ajax.md#swagger-maven-dependency)
       - [Using the "Try it out" feature](./README.files/Spring-REST-jQuery-&-Ajax.md#using-the-try-it-out-feature)
       - [Enhancing the API documentation](./README.files/Spring-REST-jQuery-&-Ajax.md#enhancing-the-api-documentation)
4. Security Part 2 - OAuth
   - Google Login
     - TBD
5. Breaking Up the application in different services 
   - TBD