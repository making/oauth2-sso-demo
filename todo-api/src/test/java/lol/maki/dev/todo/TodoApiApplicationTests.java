package lol.maki.dev.todo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "logging.level.org.springframework.web.client.RestTemplate=info",
				"spring.http.client.redirects=dont_follow" })
@Import({ TestcontainersConfiguration.class, MockConfig.class })
@Testcontainers(disabledWithoutDocker = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TodoApiApplicationTests {

	RestClient restClient;

	Function<Set<String>, String> accessTokenSupplier;

	@BeforeEach
	void setUp(@LocalServerPort int port, @Autowired RestClient.Builder builder,
			@Autowired LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor,
			@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUrl) {
		RestClient rest = builder.requestInterceptor(logbookClientHttpRequestInterceptor)
			.defaultStatusHandler(s -> true, (request, response) -> {
				/* no-op */})
			.build();
		this.accessTokenSupplier = scopes -> OAuth2.authorizationCodeFlow(URI.create(issuerUrl), rest,
				new OAuth2.User("test@example.com", "test"), new OAuth2.Client("todo-frontend", "secret"),
				URI.create("http://localhost:8080/login/oauth2/code/todo-frontend"), scopes);
		this.restClient = builder.baseUrl("http://localhost:" + port).build();
	}

	@Test
	@Order(1)
	void shouldCreateTodoWithSufficientScope() {
		String accessToken = this.accessTokenSupplier.apply(Set.of("todo:write"));
		{
			ResponseEntity<Todo> response = this.restClient.post()
				.uri("/todos")
				.contentType(MediaType.APPLICATION_JSON)
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.body("""
						{"todoTitle": "Hello World!"}
						""")
				.retrieve()
				.toEntity(Todo.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
			assertThat(response.getBody()).isEqualTo(TodoBuilder.todo()
				.todoId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
				.todoTitle("Hello World!")
				.finished(false)
				.createdAt(Instant.parse("2024-11-01T00:00:00Z"))
				.createdBy("test@example.com")
				.updatedAt(Instant.parse("2024-11-01T00:00:00Z"))
				.updatedBy("test@example.com")
				.build());
		}
		{
			ResponseEntity<Todo> response = this.restClient.post()
				.uri("/todos")
				.contentType(MediaType.APPLICATION_JSON)
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.body("""
						{"todoTitle": "Test Todo!"}
						""")
				.retrieve()
				.toEntity(Todo.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
			assertThat(response.getBody()).isEqualTo(TodoBuilder.todo()
				.todoId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
				.todoTitle("Test Todo!")
				.finished(false)
				.createdAt(Instant.parse("2024-11-01T00:00:01Z"))
				.createdBy("test@example.com")
				.updatedAt(Instant.parse("2024-11-01T00:00:01Z"))
				.updatedBy("test@example.com")
				.build());
		}
	}

	@Test
	void shouldNotCreateTodoWithInSufficientScope() {
		String accessToken = this.accessTokenSupplier.apply(Set.of("todo:read"));
		ResponseEntity<Todo> response = this.restClient.post()
			.uri("/todos")
			.contentType(MediaType.APPLICATION_JSON)
			.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
			.body("""
					{"todoTitle": "Hello World!"}
					""")
			.retrieve()
			.toEntity(Todo.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).isEqualToIgnoringNewLines(
				"""
						Bearer error="insufficient_scope", error_description="The request requires higher privileges than provided by the access token.", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"
						""");
	}

	@Test
	@Order(2)
	void shouldRetrieveTodoListWithSufficientScope() {
		String accessToken = this.accessTokenSupplier.apply(Set.of("todo:read"));
		ResponseEntity<List<Todo>> response = this.restClient.get()
			.uri("/todos")
			.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
			.retrieve()
			.toEntity(new ParameterizedTypeReference<>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).containsExactly(
				TodoBuilder.todo()
					.todoId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
					.todoTitle("Hello World!")
					.finished(false)
					.createdAt(Instant.parse("2024-11-01T00:00:00Z"))
					.createdBy("test@example.com")
					.updatedAt(Instant.parse("2024-11-01T00:00:00Z"))
					.updatedBy("test@example.com")
					.build(),
				TodoBuilder.todo()
					.todoId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
					.todoTitle("Test Todo!")
					.finished(false)
					.createdAt(Instant.parse("2024-11-01T00:00:01Z"))
					.createdBy("test@example.com")
					.updatedAt(Instant.parse("2024-11-01T00:00:01Z"))
					.updatedBy("test@example.com")
					.build());
	}

	@Test
	void shouldNotRetrieveTodoListWithInSufficientScope() {
		String accessToken = this.accessTokenSupplier.apply(Set.of("todo:write"));
		ResponseEntity<List<Todo>> response = this.restClient.get()
			.uri("/todos")
			.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
			.retrieve()
			.toEntity(new ParameterizedTypeReference<>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).isEqualToIgnoringNewLines(
				"""
						Bearer error="insufficient_scope", error_description="The request requires higher privileges than provided by the access token.", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"
						""");
	}

	@Test
	@Order(2)
	void shouldRetrieveSingleTodoWithSufficientScope() {
		String accessToken = this.accessTokenSupplier.apply(Set.of("todo:read"));
		{
			ResponseEntity<Todo> response = this.restClient.get()
				.uri("/todos/{todoId}", "00000000-0000-0000-0000-000000000001")
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.retrieve()
				.toEntity(Todo.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualTo(TodoBuilder.todo()
				.todoId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
				.todoTitle("Hello World!")
				.finished(false)
				.createdAt(Instant.parse("2024-11-01T00:00:00Z"))
				.createdBy("test@example.com")
				.updatedAt(Instant.parse("2024-11-01T00:00:00Z"))
				.updatedBy("test@example.com")
				.build());
		}
		{
			ResponseEntity<Todo> response = this.restClient.get()
				.uri("/todos/{todoId}", "00000000-0000-0000-0000-000000000002")
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.retrieve()
				.toEntity(Todo.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualTo(TodoBuilder.todo()
				.todoId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
				.todoTitle("Test Todo!")
				.finished(false)
				.createdAt(Instant.parse("2024-11-01T00:00:01Z"))
				.createdBy("test@example.com")
				.updatedAt(Instant.parse("2024-11-01T00:00:01Z"))
				.updatedBy("test@example.com")
				.build());
		}
	}

	@Test
	void shouldReturnNotFoundForNonExistentTodo() {
		String accessToken = this.accessTokenSupplier.apply(Set.of("todo:read"));
		String todoId = UUID.randomUUID().toString();
		ResponseEntity<JsonNode> response = this.restClient.get()
			.uri("/todos/{todoId}", todoId)
			.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
			.retrieve()
			.toEntity(JsonNode.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().get("message"))
			.isEqualTo(new TextNode("Todo not found: todoId=%s".formatted(todoId)));
	}

	@Test
	void shouldReturnBadRequestForInvalidTodoCreate() {
		String accessToken = this.accessTokenSupplier.apply(Set.of("todo:write"));
		ResponseEntity<JsonNode> response = this.restClient.post()
			.uri("/todos")
			.contentType(MediaType.APPLICATION_JSON)
			.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
			.body("""
					{"todoTitle": ""}
					""")
			.retrieve()
			.toEntity(JsonNode.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().get("message")).isEqualTo(new TextNode("Validation failed"));
		assertThat(response.getBody().has("violations")).isTrue();
		assertThat(response.getBody().get("violations").size()).isEqualTo(1);
		assertThat(response.getBody().get("violations").get(0).get("defaultMessage"))
			.isEqualTo(new TextNode("\"todoTitle\" must not be blank"));
	}

	@Test
	@Order(3)
	void shouldUpdateTodoWithSufficientScope() {
		String accessToken = this.accessTokenSupplier.apply(Set.of("todo:write", "todo:read"));
		{
			ResponseEntity<Todo> response = this.restClient.patch()
				.uri("/todos/{todoId}", "00000000-0000-0000-0000-000000000001")
				.contentType(MediaType.APPLICATION_JSON)
				.body("""
						{"finished": true, "todoTitle": "Hello World!!"}
						""")
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.retrieve()
				.toEntity(Todo.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualTo(TodoBuilder.todo()
				.todoId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
				.todoTitle("Hello World!!")
				.finished(true)
				.createdAt(Instant.parse("2024-11-01T00:00:00Z"))
				.createdBy("test@example.com")
				.updatedAt(Instant.parse("2024-11-01T00:00:02Z"))
				.updatedBy("test@example.com")
				.build());
		}
		{
			ResponseEntity<Todo> response = this.restClient.get()
				.uri("/todos/{todoId}", "00000000-0000-0000-0000-000000000001")
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.retrieve()
				.toEntity(Todo.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualTo(TodoBuilder.todo()
				.todoId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
				.todoTitle("Hello World!!")
				.finished(true)
				.createdAt(Instant.parse("2024-11-01T00:00:00Z"))
				.createdBy("test@example.com")
				.updatedAt(Instant.parse("2024-11-01T00:00:02Z"))
				.updatedBy("test@example.com")
				.build());
		}
	}

	@Test
	@Order(4)
	void shouldReturnBadRequestForInvalidTodoUpdate() {
		String accessToken = this.accessTokenSupplier.apply(Set.of("todo:write"));
		ResponseEntity<JsonNode> response = this.restClient.patch()
			.uri("/todos/{todoId}", "00000000-0000-0000-0000-000000000001")
			.contentType(MediaType.APPLICATION_JSON)
			.body("""
					{"finished": "bar", "todoTitle": "%s"}
					""".formatted("a".repeat(256)))
			.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
			.retrieve()
			.toEntity(JsonNode.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().get("message")).isEqualTo(new TextNode("Validation failed"));
		assertThat(response.getBody().has("violations")).isTrue();
		assertThat(response.getBody().get("violations").size()).isEqualTo(2);
		assertThat(response.getBody().get("violations").get(0).get("defaultMessage")).isEqualTo(
				new TextNode("The size of \"todoTitle\" must be less than or equal to 255. The given size is 256"));
		assertThat(response.getBody().get("violations").get(1).get("defaultMessage"))
			.isEqualTo(new TextNode("\"finished\" must be one of the following values: [true, false]"));
	}

	@Test
	@Order(4)
	void shouldReturnBadRequestForNullTodoUpdate() {
		String accessToken = this.accessTokenSupplier.apply(Set.of("todo:write"));
		ResponseEntity<JsonNode> response = this.restClient.patch()
			.uri("/todos/{todoId}", "00000000-0000-0000-0000-000000000001")
			.contentType(MediaType.APPLICATION_JSON)
			.body("""
					{}
					""")
			.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
			.retrieve()
			.toEntity(JsonNode.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().get("message")).isEqualTo(new TextNode("Validation failed"));
		assertThat(response.getBody().has("violations")).isTrue();
		assertThat(response.getBody().get("violations").size()).isEqualTo(2);
		assertThat(response.getBody().get("violations").get(0).get("defaultMessage"))
			.isEqualTo(new TextNode("\"todoTitle\" must not be blank"));
		assertThat(response.getBody().get("violations").get(1).get("defaultMessage"))
			.isEqualTo(new TextNode("\"finished\" must not be null"));
	}

	@Test
	void shouldNotUpdateTodoWithInSufficientScope() {
		String accessToken = this.accessTokenSupplier.apply(Set.of("todo:read"));
		{
			ResponseEntity<Todo> response = this.restClient.patch()
				.uri("/todos/{todoId}", "00000000-0000-0000-0000-000000000001")
				.contentType(MediaType.APPLICATION_JSON)
				.body("""
						{"finished": true, "todoTitle": "Hello World!!"}
						""")
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.retrieve()
				.toEntity(Todo.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).isEqualToIgnoringNewLines(
					"""
							Bearer error="insufficient_scope", error_description="The request requires higher privileges than provided by the access token.", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"
							""");
		}
	}

	@Test
	@Order(3)
	void shouldDeleteTodoWithSufficientScope() {
		String accessToken = this.accessTokenSupplier.apply(Set.of("todo:write", "todo:read"));
		{
			ResponseEntity<Todo> response = this.restClient.delete()
				.uri("/todos/{todoId}", "00000000-0000-0000-0000-000000000002")
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.retrieve()
				.toEntity(Todo.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		}
		{
			ResponseEntity<Todo> response = this.restClient.get()
				.uri("/todos/{todoId}", "00000000-0000-0000-0000-000000000002")
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.retrieve()
				.toEntity(Todo.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		}
	}

	@Test
	void shouldNotDeleteTodoWithInSufficientScope() {
		String accessToken = this.accessTokenSupplier.apply(Set.of("todo:read"));
		{
			ResponseEntity<Todo> response = this.restClient.delete()
				.uri("/todos/{todoId}", "00000000-0000-0000-0000-000000000001")
				.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
				.retrieve()
				.toEntity(Todo.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).isEqualToIgnoringNewLines(
					"""
							Bearer error="insufficient_scope", error_description="The request requires higher privileges than provided by the access token.", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"
							""");
		}
	}

}