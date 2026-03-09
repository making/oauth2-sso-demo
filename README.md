# OAuth2/OIDC SSO Demo with Spring Boot + Spring Security

This demo app consists of following three components:

* [Authorization](authorization) ... OAuth2/OIDC Authorization Server. Using [Spring Authorization Server](https://docs.spring.io/spring-security/reference/7.0/servlet/oauth2/authorization-server/index.html).
* [Todo API](todo-api) ... OAuth2 Resource Server. Provides REST API. Using [Spring Security's OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/7.0/servlet/oauth2/resource-server/index.html).
* [Todo Frontend](todo-frontend) ... Web UI (React + Spring MVC) backed by [Spring Security's OAuth2 Login](https://docs.spring.io/spring-security/reference/7.0/servlet/oauth2/login/index.html) + [OAuth 2.0 Client](https://docs.spring.io/spring-security/reference/7.0/servlet/oauth2/client/index.html).
* [Todo Frontend WebFlux](todo-frontend) ... Web UI (React + Spring WebFlux) backed by [Spring Security's OAuth2 Login](https://docs.spring.io/spring-security/reference/7.0/reactive/oauth2/login/index.html) + [OAuth 2.0 Client](https://docs.spring.io/spring-security/reference/7.0/reactive/oauth2/client/index.html).

## How to run

Java 21+ and docker are required.

```
./mvnw spring-boot:run -f authorization -Dspring-boot.run.arguments="--spring.docker.compose.enabled=true --spring.docker.compose.file=$(pwd)/compose.yaml"
./mvnw spring-boot:run -f todo-api -Dspring-boot.run.arguments="--spring.docker.compose.enabled=true --spring.docker.compose.file=$(pwd)/compose.yaml"
./mvnw spring-boot:run -f todo-frontend -Dspring-boot.run.arguments="--spring.docker.compose.enabled=true --spring.docker.compose.file=$(pwd)/compose.yaml"
or
./mvnw spring-boot:run -f todo-frontend-webflux -Dspring-boot.run.arguments="--spring.docker.compose.enabled=true --spring.docker.compose.file=$(pwd)/compose.yaml"
```

Visit http://localhost:8080 (todo-frontend). The first time you visit, you will be redirected to http://127.0.0.1:9000 (authorization).

<img width="1024" alt="Image" src="https://github.com/user-attachments/assets/f770c7a3-1cd3-425b-ba35-e884641f8428" />

* Username: `john@example.com`
* Password: `password`

<img width="1024" alt="Image" src="https://github.com/user-attachments/assets/962fadf0-f4ef-4a72-9ec8-f1a3e46a54e4" />

Feel free to add new Todos or complete them.

<img width="1024" alt="Image" src="https://github.com/user-attachments/assets/16808462-711d-49b6-af6d-2319e720ddac" />

You can see the tracing via Zipkin (http://localhost:9411)

<img width="1024" alt="Image" src="https://github.com/user-attachments/assets/77767d78-8bbb-4910-b54f-43b4d2df8068" />
<img width="1024" alt="Image" src="https://github.com/user-attachments/assets/0071db90-790c-4835-8cb7-2223d0eb0073" />

Docker Compose will automatically start up when the app is started, but it will not automatically shut down, so if you do not need Zipkin after stopping the app, please shut down Docker Compose as well.

```
pkill -TERM java
docker compose down
```