# OAuth2 SSO Demo with Spring Boot + Spring Security OAuth2

This demo app consists of following three components:

* Authorization ... OAuth2 [Authorization Server](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security-oauth2-authorization-server). Google API is used.
* [Resource](resource) ... OAuth2 [Resource Server](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security-oauth2-resource-server). Provides REST API.
* [UI](ui) ... Web UI using [SSO](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security-oauth2-single-sign-on) based on OAuth2

![image](https://qiita-image-store.s3.amazonaws.com/0/1852/8310be66-f56a-677b-0554-b4d51dfe05d8.png)


## Variants

* [JWT version](https://github.com/making/oauth2-sso-demo/tree/jwt)
* [Zuul integration](https://github.com/making/oauth2-sso-demo/tree/zuul) using Ajax
* [Use GitHub API instead of Authorization Server](https://github.com/making/oauth2-sso-demo/tree/github)
* [Use Google+ API instead of Authorization Server](https://github.com/making/oauth2-sso-demo/tree/google)