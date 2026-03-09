package lol.maki.dev.authorization;

import java.nio.file.Path;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lol.maki.dev.authorization.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests verifying that {@link JdbcOAuth2AuthorizationService} can serialize and
 * deserialize {@link OAuth2Authorization} containing a {@link WebAuthnAuthentication}
 * principal.
 *
 * <p>
 * Without the custom {@link tools.jackson.databind.json.JsonMapper} configuration (added
 * in {@link SecurityConfig}), the default {@code JdbcOAuth2AuthorizationService} fails to
 * deserialize {@link WebAuthnAuthentication} because its {@code PolymorphicTypeValidator}
 * does not allow it.
 */
class WebAuthnJdbcAuthorizationServiceTests {

	@TempDir
	Path tempDir;

	private JdbcTemplate jdbcTemplate;

	private RegisteredClientRepository registeredClientRepository;

	private RegisteredClient registeredClient;

	@BeforeEach
	void setUp() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.sqlite.JDBC");
		dataSource.setUrl("jdbc:sqlite:" + this.tempDir.resolve("test.db").toAbsolutePath());
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript(new ClassPathResource(
				"org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql"));
		populator.addScript(new ClassPathResource(
				"org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql"));
		populator.execute(dataSource);
		this.registeredClientRepository = new JdbcRegisteredClientRepository(this.jdbcTemplate);
		this.registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
			.clientId("test-client")
			.clientSecret("{noop}secret")
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUri("http://localhost:8080/login/oauth2/code/test")
			.scope("openid")
			.build();
		this.registeredClientRepository.save(this.registeredClient);
	}

	@Test
	void defaultServiceFailsToDeserializeWebAuthnAuthentication() {
		// Default JdbcOAuth2AuthorizationService (without WebAuthn-aware JsonMapper)
		JdbcOAuth2AuthorizationService service = new JdbcOAuth2AuthorizationService(this.jdbcTemplate,
				this.registeredClientRepository);

		OAuth2Authorization authorization = createAuthorizationWithWebAuthnPrincipal();
		// Save succeeds (serialization works)
		service.save(authorization);

		// findById fails because deserialization rejects WebAuthnAuthentication
		assertThatThrownBy(() -> service.findById(authorization.getId())).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(
					"Could not resolve type id 'org.springframework.security.web.webauthn.authentication.WebAuthnAuthentication' as a subtype of `java.lang.Object`: Configured `PolymorphicTypeValidator` (of type `tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator`) denied resolution")
			.hasMessageContaining("WebAuthnAuthentication")
			.hasMessageContaining("PolymorphicTypeValidator");
	}

	@Test
	void webAuthnAwareServiceCanSerializeAndDeserializeWebAuthnAuthentication() {
		JdbcOAuth2AuthorizationService service = SecurityConfig
			.createWebAuthnAwareAuthorizationService(this.jdbcTemplate, this.registeredClientRepository);

		OAuth2Authorization authorization = createAuthorizationWithWebAuthnPrincipal();
		service.save(authorization);

		// findById succeeds
		OAuth2Authorization found = service.findById(authorization.getId());
		assertThat(found).isNotNull();
		assertThat(found.getPrincipalName()).isEqualTo("john@example.com");

		// Verify the principal is correctly deserialized
		Object principal = found.getAttribute(Principal.class.getName());
		assertThat(principal).isInstanceOf(WebAuthnAuthentication.class);
		WebAuthnAuthentication webAuthnAuth = (WebAuthnAuthentication) principal;
		assertThat(webAuthnAuth.getName()).isEqualTo("john@example.com");
		assertThat(webAuthnAuth.getPrincipal()).isInstanceOf(ImmutablePublicKeyCredentialUserEntity.class);
		assertThat(webAuthnAuth.getPrincipal().getId()).isNotNull();
	}

	private OAuth2Authorization createAuthorizationWithWebAuthnPrincipal() {
		WebAuthnAuthentication webAuthnAuthentication = new WebAuthnAuthentication(
				ImmutablePublicKeyCredentialUserEntity.builder()
					.name("john@example.com")
					.id(new Bytes("test-user-id".getBytes()))
					.displayName("John")
					.build(),
				List.of(new SimpleGrantedAuthority("ROLE_USER")));

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plusSeconds(300);
		OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode("test-code", issuedAt, expiresAt);

		OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
			.clientId(this.registeredClient.getClientId())
			.authorizationUri("http://localhost:9000/oauth2/authorize")
			.redirectUri("http://localhost:8080/login/oauth2/code/test")
			.scope("openid")
			.state("test-state")
			.build();

		return OAuth2Authorization.withRegisteredClient(this.registeredClient)
			.id(UUID.randomUUID().toString())
			.principalName("john@example.com")
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.authorizedScopes(this.registeredClient.getScopes())
			.attribute(Principal.class.getName(), webAuthnAuthentication)
			.attribute(OAuth2AuthorizationRequest.class.getName(), authorizationRequest)
			.token(authorizationCode)
			.build();
	}

}
