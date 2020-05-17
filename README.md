# OAuth2/OIDC SSO Demo with Spring Boot + Spring Security + Spring Cloud Gateway

This demo app consists of following three components:

* [Authorization](authorization) ... OAuth2 Authorization Server which implements some OIDC features that Spring Security requires. 
* [Todo API](todo-api) ... OAuth2 Resource Server. Provides REST API.
* [Todo Frontend](todo-frontend) ... Web UI (React) backed by Spring Cloud Gateway + [Token Relay](https://cloud.spring.io/spring-cloud-security/reference/html/#_client_token_relay_in_spring_cloud_gateway. 

## Resource Owner Password Credentials Flow

```
ACCESS_TOKEN=$(curl -s http://127.0.0.1:9999/oauth/token -u todo:todo -d grant_type=password -d username=foo@example.com -d password=password  | jq -r .access_token)

TODO_ID=$(curl -s localhost:8082/todos -H "Authorization: Bearer ${ACCESS_TOKEN}" -H "Content-Type: application/json" -d '{"todoTitle": "Demo"}' | jq -r .todoId)
curl -s localhost:8082/todos -H "Authorization: Bearer ${ACCESS_TOKEN}"
curl -s localhost:8082/todos/${TODO_ID} -H "Authorization: Bearer ${ACCESS_TOKEN}"
curl -s -X PUT localhost:8082/todos/${TODO_ID} -H "Authorization: Bearer ${ACCESS_TOKEN}" -H "Content-Type: application/json" -d '{"finished": "true"}'
curl -s -X DELETE localhost:8082/todos/${TODO_ID} -H "Authorization: Bearer ${ACCESS_TOKEN}"
curl -s localhost:8082/todos -H "Authorization: Bearer ${ACCESS_TOKEN}"```