# Simple Web Application with Simple Security
#### A study on how to create a Spring MVC project using old stuff (JSP, jQuery, Ajax), REST and Security.

The main idea of this proof of Concept project is to create an "Old School" Java Web Application using the newest Spring 
Boot version and its security features. With "Old School" I mean JSP server-side rendered web pages, no Thymeleaf (like
that's new school) and no Single Page Applications (SPA) (e.g. React). But I would like to use some asynchronous REST 
calls using old school Ajax with jQuery.

### Steps
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
       - [Security](./README.files/Spring-Security.md#security)
   - [Configuration](./README.files/Spring-Security.md#configuration)
       - [Default Login](./README.files/Spring-Security.md#default-login)
       - [Hard coding credentials](./README.files/Spring-Security.md#hard-coding-credentials)
   - [Restricting access by role](./README.files/Spring-Security.md#restricting-access-by-role)
   - [Allowing anonymous access](./README.files/Spring-Security.md#allowing-anonymous-access)
   - [Logout redirection](./README.files/Spring-Security.md#logout-redirection)
   - [Last touch](./README.files/Spring-Security.md#last-touch)
       - [Other secured and unsecured pages](./README.files/Spring-Security.md#other-secured-and-unsecured-pages)
