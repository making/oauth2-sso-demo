# OAuth2/OIDC SSO Demo with Spring Boot + Spring Security

This demo app consists of following three components:

* [Authorization](authorization) ... OAuth2/OIDC Authorization Server. Using [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/index.html).
* [Todo API](todo-api) ... OAuth2 Resource Server. Provides REST API. Using [Spring Security's OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/6.4/servlet/oauth2/resource-server/index.html).
* [Todo Frontend](todo-frontend) ... Web UI (React) backed by [Spring Security's OAuth2 Login](https://docs.spring.io/spring-security/reference/6.4/servlet/oauth2/login/index.html) + [OAuth 2.0 Client](https://docs.spring.io/spring-security/reference/6.4/servlet/oauth2/client/index.html).

## How to run

Java 21+ and docker are required.

```
./mvnw spring-boot:run -f authorization -Dspring-boot.run.arguments="--spring.docker.compose.enabled=true --spring.docker.compose.file=$(pwd)/compose.yaml"
./mvnw spring-boot:run -f todo-api -Dspring-boot.run.arguments="--spring.docker.compose.enabled=true --spring.docker.compose.file=$(pwd)/compose.yaml"
./mvnw spring-boot:run -f todo-frontend -Dspring-boot.run.arguments="--spring.docker.compose.enabled=true --spring.docker.compose.file=$(pwd)/compose.yaml"
```

Visit http://localhost:8080 (todo-frontend). The first time you visit, you will be redirected to http://127.0.0.1:9000 (authorization).

<img width="1024" alt="image" src="https://github.com/user-attachments/assets/5f2764a8-4fde-41be-b0e6-d21f04d7fdeb">

* Username: `john@example.com`
* Password: `password`

<img width="1024" alt="image" src="https://github.com/user-attachments/assets/c4933de2-1895-4c08-8f11-d6c67dcc2f34">

Feel free to add new Todos or complete them.

<img width="1024" alt="image" src="https://github.com/user-attachments/assets/9e115ad3-3e5a-4d69-a418-695c1e3e15e7">

You can see the tracing via Zipkin (http://localhost:9411)

<img width="1024" alt="image" src="https://github.com/user-attachments/assets/1cdfaec6-affe-4baf-aec2-6592c8716e3f">
<img width="1024" alt="image" src="https://github.com/user-attachments/assets/4a0b15e5-a89f-4010-bac7-3f877208c83d">

Docker Compose will automatically start up when the app is started, but it will not automatically shut down, so if you do not need Zipkin after stopping the app, please shut down Docker Compose as well.

```
pkill -TERM java
docker compose down
```