# OAuth2/OIDC SSO Demo with Spring Boot + Spring Security + Spring Cloud Gateway

This demo app consists of following three components:

* [Authorization](authorization) ... OAuth2 Authorization Server which implements some OIDC features that Spring Security requires. Using [legacy Spring Security OAuth2](https://docs.spring.io/spring-security-oauth2-boot/docs/2.3.x-SNAPSHOT/reference/html5/).
* [Todo API](todo-api) ... OAuth2 Resource Server. Provides REST API. Using [Spring Security 5's OAuth2 Resource Server functionality](https://docs.spring.io/spring-security/site/docs/5.3.2.RELEASE/reference/html5/#oauth2resourceserver).
* [Todo Frontend](todo-frontend) ... Web UI (React) backed by [Spring Security 5's OAuth2 Login functionality](https://docs.spring.io/spring-security/site/docs/5.3.2.RELEASE/reference/html5/#oauth2login) + [Spring Cloud Gateway's Token Relay](https://cloud.spring.io/spring-cloud-security/reference/html/#_client_token_relay_in_spring_cloud_gateway). 

> ⚠️ Spring Security OAuth [will reach end-of-life in May 2022](https://spring.io/blog/2020/05/07/end-of-life-for-spring-security-oauth).

## Authorization Code Flow

![image](https://user-images.githubusercontent.com/106908/82171880-78de2f00-9903-11ea-9302-a30b85bed3db.png)

## Resource Owner Password Credentials Flow

```
ACCESS_TOKEN=$(curl -s http://127.0.0.1:9999/oauth/token -u todo:todo -d grant_type=password -d username=foo@example.com -d password=password  | jq -r .access_token)

TODO_ID=$(curl -s localhost:8082/todos -H "Authorization: Bearer ${ACCESS_TOKEN}" -H "Content-Type: application/json" -d '{"todoTitle": "Demo"}' | jq -r .todoId)
curl -s localhost:8082/todos -H "Authorization: Bearer ${ACCESS_TOKEN}"
curl -s localhost:8082/todos/${TODO_ID} -H "Authorization: Bearer ${ACCESS_TOKEN}"
curl -s -X PUT localhost:8082/todos/${TODO_ID} -H "Authorization: Bearer ${ACCESS_TOKEN}" -H "Content-Type: application/json" -d '{"finished": "true"}'
curl -s -X DELETE localhost:8082/todos/${TODO_ID} -H "Authorization: Bearer ${ACCESS_TOKEN}"
curl -s localhost:8082/todos -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

## Build

```
./mvnw clean package -f authorization/pom.xml -DskipTests
./mvnw clean package -f todo-api/pom.xml -DskipTests
./mvnw clean package -f todo-frontend/pom.xml -DskipTests
```

## Deploy to Cloud Foundry

```
## Fix name and OAUTH_CLIENTS_0_ADDITIONALINFORMATION_URL for your env
cf push -f authorization/manifest.yml 
```

```
## Fix name and SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI for your env
cf push -f todo-api/manifest.yml 
```

```
## Fix name, SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_DEMO_ISSUER_URI and SPRING_CLOUD_GATEWAY_ROUTES_0_URI for your env
cf push -f todo-frontend/manifest.yml 
```

## Demo Site

* url: https://todo.apps.pcfone.io
* user: `demo@example.com / password` or `john@example.com / password`
