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
{"access_token":"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0NjMzNzQzNDYsInVzZXJfbmFtZSI6InVzZXIiLCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOIiwiUk9MRV9VU0VSIl0sImp0aSI6IjhlZDA0MDM2LWMwYjItNDJhZC1hZThmLTNiMTg3NGE5YjlmMiIsImNsaWVudF9pZCI6ImRlbW8iLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXX0.0p9uddJWyKafC0pzubQdCJR4wd9jAZdi07xOZfT8H_mQa629ybz-hT9KqyTu4uf6JdInIovmb6YkRS3OixBfStyULKbBKKdQqhuir_IYHBaxkMyE4CewnXu9c1VpF6qhzL8ucXZ7xiT9eRDhbxwQhCb305f1v4yZMvdPw5ZSLm9Fje6mwCIrq-uzqQlaPP-zvr1_5wkqNc5fy0jjQkfAKTSTKLUBybqG80bmAD9rB6hB--QnIYjtdsT8jBwoH03HHBUL31ABb2lxGXNc248BwCkfmYUBwsFt32eeT2adfhLnfZ2L7noBJVLV3E3AioMbkapFymYZGv7qlG-hYFXozw","token_type":"bearer","refresh_token":"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJ1c2VyIiwic2NvcGUiOlsicmVhZCIsIndyaXRlIl0sImF0aSI6IjhlZDA0MDM2LWMwYjItNDJhZC1hZThmLTNiMTg3NGE5YjlmMiIsImV4cCI6MTQ2NTk2Mjc0NiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9BRE1JTiIsIlJPTEVfVVNFUiJdLCJqdGkiOiJmZDFiOWI2ZS0xYzQ3LTRmOGQtYjA3Mi1kYTUxM2IwZmJjMzIiLCJjbGllbnRfaWQiOiJkZW1vIn0.ImHqIMhltBHKga2JgO2S6MXwzptUGXQ2JTrzDKV2V3H2xDEvFXpxfagZDHkV8ru9LqJC3o7OvcCtj8OPeO1mUgu7Qf7T0DzcPWV0Ro5jdTqypUBTmUFGoPNkFrzyCxgZ1vyxx7vwDeFQfKCEa4nwmYD24DzROjbcuakaMtYwGR_s3o1Jy2KL56n7IWsysLxKNjZX7mfG6XLYuCbxvXS-ReltFBPaa0eV631uXm3ydph-IJbIGPHTpCG3niiNAmmiF-5XH0PF0spKuM_pxHXJsMEwZlERyFFfJj2PKox--1FRAtRFKIfQiv0uGZJxR6NB7az0R2qbtQTEhLXW6YudPA","expires_in":3599,"scope":"read write","jti":"8ed04036-c0b2-42ad-ae8f-3b1874a9b9f2"}```
```

Post a Resource

``` console
$ curl -H 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0NjMzNzQzNDYsInVzZXJfbmFtZSI6InVzZXIiLCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOIiwiUk9MRV9VU0VSIl0sImp0aSI6IjhlZDA0MDM2LWMwYjItNDJhZC1hZThmLTNiMTg3NGE5YjlmMiIsImNsaWVudF9pZCI6ImRlbW8iLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXX0.0p9uddJWyKafC0pzubQdCJR4wd9jAZdi07xOZfT8H_mQa629ybz-hT9KqyTu4uf6JdInIovmb6YkRS3OixBfStyULKbBKKdQqhuir_IYHBaxkMyE4CewnXu9c1VpF6qhzL8ucXZ7xiT9eRDhbxwQhCb305f1v4yZMvdPw5ZSLm9Fje6mwCIrq-uzqQlaPP-zvr1_5wkqNc5fy0jjQkfAKTSTKLUBybqG80bmAD9rB6hB--QnIYjtdsT8jBwoH03HHBUL31ABb2lxGXNc248BwCkfmYUBwsFt32eeT2adfhLnfZ2L7noBJVLV3E3AioMbkapFymYZGv7qlG-hYFXozw' \
       -H 'Content-Type: application/json' \
       -d '{"text" : "Hello World!"}' \
       localhost:7777/api/messages
{"text":"Hello World!","username":"user","createdAt":"2016-05-16T12:53:03.263"}
```

Get Resources

``` console
$ curl -H 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0NjMzNzQzNDYsInVzZXJfbmFtZSI6InVzZXIiLCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOIiwiUk9MRV9VU0VSIl0sImp0aSI6IjhlZDA0MDM2LWMwYjItNDJhZC1hZThmLTNiMTg3NGE5YjlmMiIsImNsaWVudF9pZCI6ImRlbW8iLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXX0.0p9uddJWyKafC0pzubQdCJR4wd9jAZdi07xOZfT8H_mQa629ybz-hT9KqyTu4uf6JdInIovmb6YkRS3OixBfStyULKbBKKdQqhuir_IYHBaxkMyE4CewnXu9c1VpF6qhzL8ucXZ7xiT9eRDhbxwQhCb305f1v4yZMvdPw5ZSLm9Fje6mwCIrq-uzqQlaPP-zvr1_5wkqNc5fy0jjQkfAKTSTKLUBybqG80bmAD9rB6hB--QnIYjtdsT8jBwoH03HHBUL31ABb2lxGXNc248BwCkfmYUBwsFt32eeT2adfhLnfZ2L7noBJVLV3E3AioMbkapFymYZGv7qlG-hYFXozw' localhost:7777/api/messages
[{"text":"Hello World!","username":"user","createdAt":"2016-05-16T12:53:03.263"}]
```

## Variants

* [JWT version](https://github.com/making/oauth2-sso-demo/tree/jwt)
* [Zuul integration](https://github.com/making/oauth2-sso-demo/tree/zuul) using Ajax
* [Use GitHub API instead of Authorization Server](https://github.com/making/oauth2-sso-demo/tree/github)
* [Use Google+ API instead of Authorization Server](https://github.com/making/oauth2-sso-demo/tree/google)