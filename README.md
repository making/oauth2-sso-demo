⚠️ **Spring Boot 2.0 / Spring Security 5.0 version is [here](https://github.com/making/demo-oauth2-login)**

# OAuth2 SSO Demo with Spring Boot + Spring Security OAuth2

This demo app consists of following three components:

* [Authorization](authorization) ... OAuth2 [Authorization Server](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security-oauth2-authorization-server)
* [Resource](resource) ... OAuth2 [Resource Server](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security-oauth2-resource-server). Provides REST API.
* [UI](ui) ... Web UI using [SSO](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security-oauth2-single-sign-on) based on OAuth2

### Authorization Code Flow

![image](https://qiita-image-store.s3.amazonaws.com/0/1852/19969057-c8d1-e2d7-fd56-82fe784e7a36.png)

### Resource Owner Password Credentials Flow

Get an Access Token

``` console
$ curl -XPOST -u demo:demo localhost:9999/uaa/oauth/token -d grant_type=password -d username=user -d password=password
{"access_token":"00bc1b1a-36be-4884-855b-c7854d7b7915","token_type":"bearer","refresh_token":"06c522b3-66fc-4de1-9a0e-cd1765f8a0a2","expires_in":43199,"scope":"read write"}
```

Post a Resource

``` console
$ curl -H 'Authorization: Bearer 00bc1b1a-36be-4884-855b-c7854d7b7915' \
       -H 'Content-Type: application/json' \
       -d '{"text" : "Hello World!"}' \
       localhost:7777/api/messages
{"text":"Hello World!","username":"user","createdAt":"2016-05-16T12:48:39.466"}
```

Get Resources

``` console
$ curl -H 'Authorization: Bearer 00bc1b1a-36be-4884-855b-c7854d7b7915' localhost:7777/api/messages
[{"text":"Hello World!","username":"user","createdAt":"2016-05-16T12:48:39.466"}]
```

## Variants

* [JWT version](https://github.com/making/oauth2-sso-demo/tree/jwt)
* [Zuul integration](https://github.com/making/oauth2-sso-demo/tree/zuul) using Ajax
* [Use GitHub API instead of Authorization Server](https://github.com/making/oauth2-sso-demo/tree/github)
* [Use Google+ API instead of Authorization Server](https://github.com/making/oauth2-sso-demo/tree/google)
