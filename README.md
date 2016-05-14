# OAuth2 SSO Demo with Spring Boot + Spring Security OAuth2

This demo app consists of following three components:

* [Authorization](authorization) ... OAuth2 [Authorization Server](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security-oauth2-authorization-server)
* [Resource](resource) ... OAuth2 [Resource Server](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security-oauth2-resource-server). Provides REST API.
* [UI](ui) ... Web UI using [SSO](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security-oauth2-single-sign-on) based on OAuth2

![image](https://qiita-image-store.s3.amazonaws.com/0/1852/19969057-c8d1-e2d7-fd56-82fe784e7a36.png)


## Variants

* [JWT version](tree/jwt)
* [Use GitHub API instead of Authorization Server](tree/github)
* [Use Google+ API instead of Authorization Server](tree/google)