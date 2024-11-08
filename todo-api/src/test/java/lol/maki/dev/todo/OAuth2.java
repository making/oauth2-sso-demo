package lol.maki.dev.todo;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

final class OAuth2 {

	record Client(String clientId, String clientSecret) {
	}

	record User(String username, String password) {
	}

	static String formLogin(URI loginUrl, RestClient restClient, User user) {
		ResponseEntity<String> loginForm = restClient.get().uri(loginUrl).retrieve().toEntity(String.class);
		String jsessionId = Arrays
			.stream(Objects.requireNonNull(loginForm.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).split("; "))
			.filter(x -> x.startsWith("JSESSIONID="))
			.collect(Collectors.joining())
			.split("=")[1];
		Matcher matcher = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"")
			.matcher(Objects.requireNonNull(loginForm.getBody()));
		String csrfToken = matcher.find() ? matcher.group(1) : "";
		ResponseEntity<String> login = restClient.post()
			.uri(loginUrl)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body("username=%s&password=%s&_csrf=%s".formatted(user.username(), user.password(), csrfToken))
			.cookie("JSESSIONID", jsessionId)
			.retrieve()
			.toEntity(String.class);
		return Arrays.stream(Objects.requireNonNull(login.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).split("; "))
			.filter(x -> x.startsWith("JSESSIONID="))
			.collect(Collectors.joining())
			.split("=")[1];
	}

	static String authorizationCodeFlow(URI issuerUrl, RestClient restClient, User user, Client client, URI redirectUri,
			Set<String> scopes) {
		String jsessionId = formLogin(URI.create(issuerUrl + "/login"), restClient, user);
		JsonNode openIdConfiguration = Objects.requireNonNull(
				restClient.get().uri(issuerUrl + "/.well-known/openid-configuration").retrieve().body(JsonNode.class));
		String tokenEndpoint = openIdConfiguration.get("token_endpoint").asText();
		String authorizationEndpoint = openIdConfiguration.get("authorization_endpoint").asText();
		ResponseEntity<String> redirectToCode = restClient.get()
			.uri(authorizationEndpoint,
					uri -> uri.queryParam("response_type", "code")
						.queryParam("client_id", client.clientId())
						.queryParam("redirect_uri", redirectUri)
						.queryParam("scope", String.join(" ", scopes))
						.build())
			.cookie("JSESSIONID", jsessionId)
			.retrieve()
			.toEntity(String.class);
		String code = UriComponentsBuilder.fromUri(Objects.requireNonNull(redirectToCode.getHeaders().getLocation()))
			.build()
			.getQueryParams()
			.getFirst("code");
		JsonNode token = restClient.post()
			.uri(tokenEndpoint)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.headers(httpHeaders -> httpHeaders.setBasicAuth(client.clientId(), client.clientSecret()))
			.body("grant_type=authorization_code&code=%s&redirect_uri=%s".formatted(code, redirectUri))
			.retrieve()
			.body(JsonNode.class);
		return Objects.requireNonNull(token).get("access_token").asText();
	}

}
