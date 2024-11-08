package lol.maki.dev.authorization;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "spring.http.client.redirects=dont_follow" })
class AuthorizationApplicationTests {

	RestClient restClient;

	@BeforeEach
	void setUp(@LocalServerPort int port, @Autowired RestClient.Builder builder,
			@Autowired LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor) {
		this.restClient = builder.requestInterceptor(logbookClientHttpRequestInterceptor)
			.defaultStatusHandler(s -> true, (request, response) -> {
				/* no-op */
			})
			.baseUrl("http://localhost:" + port)
			.build();
	}

	ResponseEntity<Void> formLogin(String username, String password) {
		ResponseEntity<String> loginForm = this.restClient.get().uri("/login").retrieve().toEntity(String.class);
		assertThat(loginForm.getStatusCode()).isEqualTo(HttpStatus.OK);
		ResponseEntity<Void> login = this.restClient.post()
			.uri("/login")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body("username=%s&password=%s&_csrf=%s".formatted(username, password, csrfToken(loginForm)))
			.cookie("JSESSIONID", jsessionId(loginForm))
			.retrieve()
			.toEntity(Void.class);
		assertThat(login.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		return login;
	}

	String jsessionId(ResponseEntity<?> response) {
		return Arrays.stream(Objects.requireNonNull(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).split("; "))
			.filter(x -> x.startsWith("JSESSIONID="))
			.collect(Collectors.joining())
			.split("=")[1];
	}

	String csrfToken(ResponseEntity<String> response) {
		Matcher matcher = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"")
			.matcher(Objects.requireNonNull(response.getBody()));
		return matcher.find() ? matcher.group(1) : "";
	}

	@Test
	void shouldObtainAccessTokenWithAuthorizationCodeFlow() {
		ResponseEntity<Void> login = formLogin("john@example.com", "password");
		ResponseEntity<Void> redirectToCode = this.restClient.get()
			.uri("/oauth2/authorize",
					uri -> uri.queryParam("response_type", "code")
						.queryParam("client_id", "todo-frontend")
						.queryParam("redirect_uri", "http://localhost:8080/login/oauth2/code/todo-frontend")
						.queryParam("scope", "todo:read todo:write openid")
						.build())
			.cookie("JSESSIONID", jsessionId(login))
			.retrieve()
			.toEntity(Void.class);
		assertThat(redirectToCode.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		URI location = redirectToCode.getHeaders().getLocation();
		assertThat(location).isNotNull();
		String code = UriComponentsBuilder.fromUri(location).build().getQueryParams().getFirst("code");
		assertThat(code).isNotEmpty();
		ResponseEntity<JsonNode> token = restClient.post()
			.uri("/oauth2/token")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.headers(httpHeaders -> httpHeaders.setBasicAuth("todo-frontend", "secret"))
			.body("grant_type=authorization_code&code=%s&redirect_uri=%s".formatted(code,
					"http://localhost:8080/login/oauth2/code/todo-frontend"))
			.retrieve()
			.toEntity(JsonNode.class);
		assertThat(token.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(token.getBody()).isNotNull();
		assertThat(token.getBody().get("access_token")).isNotNull();
		String accessToken = token.getBody().get("access_token").textValue();
		assertThat(accessToken).isNotEmpty();
		assertThat(token.getBody().get("refresh_token")).isNotNull();
		assertThat(token.getBody().get("refresh_token").textValue()).isNotEmpty();
		assertThat(token.getBody().get("scope")).isNotNull();
		String scope = token.getBody().get("scope").textValue();
		assertThat(scope).contains("todo:read", "todo:write", "openid");
		ResponseEntity<JsonNode> userInfo = this.restClient.get()
			.uri("/userinfo")
			.headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
			.retrieve()
			.toEntity(JsonNode.class);
		assertThat(userInfo.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(userInfo.getBody()).isNotNull();
		assertThat(userInfo.getBody().get("sub")).isNotNull();
		assertThat(userInfo.getBody().get("sub").textValue()).isEqualTo("john@example.com");
	}

	@Test
	void shouldFailLoginWithInvalidCredentials() {
		String username = "john@example.com";
		String password = "qwerty";
		ResponseEntity<String> loginForm = this.restClient.get().uri("/login").retrieve().toEntity(String.class);
		assertThat(loginForm.getStatusCode()).isEqualTo(HttpStatus.OK);
		String jsessionId = jsessionId(loginForm);
		ResponseEntity<Void> login = this.restClient.post()
			.uri("/login")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body("username=%s&password=%s&_csrf=%s".formatted(username, password, csrfToken(loginForm)))
			.cookie("JSESSIONID", jsessionId)
			.retrieve()
			.toEntity(Void.class);
		URI location = login.getHeaders().getLocation();
		assertThat(location).isNotNull();
		assertThat(location.toString()).endsWith("?error");
		ResponseEntity<String> loginFailed = this.restClient.get()
			.uri(location)
			.cookie("JSESSIONID", jsessionId)
			.retrieve()
			.toEntity(String.class);
		assertThat(loginFailed.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(loginFailed.getBody()).containsIgnoringNewLines("""
				<div class="error-message">
				""");
	}

}
