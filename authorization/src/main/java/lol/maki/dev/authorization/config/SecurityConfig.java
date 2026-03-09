package lol.maki.dev.authorization.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.util.List;
import java.util.Map;

import lol.maki.dev.authorization.JwtProperties;
import lol.maki.dev.authorization.WebAuthnProps;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationParametersMapper;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.webauthn.jackson.WebauthnJacksonModule;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

	private final WebAuthnProps webAuthnProps;

	public SecurityConfig(WebAuthnProps webAuthnProps) {
		this.webAuthnProps = webAuthnProps;
	}

	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		return http.oauth2AuthorizationServer((authorizationServer) -> {
			http.securityMatcher(authorizationServer.getEndpointsMatcher());
			authorizationServer.authorizationEndpoint(endpoint -> endpoint.consentPage("/oauth2/consent"))
				.oidc(Customizer.withDefaults());
		})
			.authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
			// Redirect to the login page when not authenticated from the
			// authorization endpoint
			.exceptionHandling((exceptions) -> exceptions.defaultAuthenticationEntryPointFor(
					new LoginUrlAuthenticationEntryPoint("/login"), new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
			.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((authorize) -> authorize.requestMatchers(EndpointRequest.toAnyEndpoint())
			.permitAll()
			.requestMatchers("/css/**", "/js/**", "/login", "/signup", "/error")
			.permitAll()
			.anyRequest()
			.authenticated())
			.formLogin(form -> form.loginPage("/login"))
			.logout(logout -> logout.logoutSuccessUrl("/login"))
			.webAuthn(webauthn -> webauthn.rpName(this.webAuthnProps.rpName())
				.rpId(this.webAuthnProps.rpId())
				.allowedOrigins(this.webAuthnProps.allowedOrigins())
				.disableDefaultRegistrationPage(true));
		return http.build();
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource(JwtProperties props) {
		List<JWK> keys = props.keySets().stream().map(keySet -> {
			try {
				return (JWK) new RSAKey.Builder(keySet.publicKey()).privateKey(keySet.privateKey())
					.keyIDFromThumbprint()
					.build();
			}
			catch (JOSEException e) {
				throw new IllegalStateException(e);
			}
		}).toList();
		return new ImmutableJWKSet<>(new JWKSet(keys));
	}

	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> jwtEncodingContextCustomizer() {
		return context -> {
			JwtClaimsSet.Builder claims = context.getClaims();
			String name = context.getPrincipal().getName();
			claims.claim("email", name); // use username as 'email' claim
		};
	}

	@Bean
	public JdbcRegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
		return new JdbcRegisteredClientRepository(jdbcTemplate);
	}

	@Bean
	public JdbcOAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate,
			RegisteredClientRepository registeredClientRepository) {
		return createWebAuthnAwareAuthorizationService(jdbcTemplate, registeredClientRepository);
	}

	public static JdbcOAuth2AuthorizationService createWebAuthnAwareAuthorizationService(JdbcTemplate jdbcTemplate,
			RegisteredClientRepository registeredClientRepository) {
		JsonMapper jsonMapper = createWebAuthnAwareJsonMapper();
		JdbcOAuth2AuthorizationService authorizationService = new JdbcOAuth2AuthorizationService(jdbcTemplate,
				registeredClientRepository);
		authorizationService.setAuthorizationRowMapper(
				new JsonMapperOAuth2AuthorizationRowMapper(registeredClientRepository, jsonMapper));
		authorizationService
			.setAuthorizationParametersMapper(new JsonMapperOAuth2AuthorizationParametersMapper(jsonMapper));
		return authorizationService;
	}

	static JsonMapper createWebAuthnAwareJsonMapper() {
		ClassLoader classLoader = SecurityConfig.class.getClassLoader();
		// Configure the type validator via WebAuthnSessionJacksonModule before
		// SecurityJacksonModules builds the PolymorphicTypeValidator
		WebAuthnSessionJacksonModule webAuthnSessionModule = new WebAuthnSessionJacksonModule();
		BasicPolymorphicTypeValidator.Builder typeValidatorBuilder = BasicPolymorphicTypeValidator.builder();
		webAuthnSessionModule.configurePolymorphicTypeValidator(typeValidatorBuilder);
		List<JacksonModule> modules = SecurityJacksonModules.getModules(classLoader, typeValidatorBuilder);
		// Add WebauthnJacksonModule (excluded by default from SecurityJacksonModules)
		modules.add(new WebauthnJacksonModule());
		modules.add(webAuthnSessionModule);
		return JsonMapper.builder().addModules(modules).build();
	}

	@Bean
	public JdbcOAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate,
			RegisteredClientRepository registeredClientRepository) {
		return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
	}

	@Bean
	public JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
		JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
		manager.setEnableUpdatePassword(true);
		return manager;
	}

	@Bean
	public UserCredentialRepository userCredentialRepository(JdbcTemplate jdbcTemplate) {
		return new JdbcUserCredentialRepository(jdbcTemplate);
	}

	@Bean
	public PublicKeyCredentialUserEntityRepository publicKeyCredentialUserEntityRepository(JdbcTemplate jdbcTemplate) {
		return new JdbcPublicKeyCredentialUserEntityRepository(jdbcTemplate);
	}

	@SuppressWarnings("deprecation")
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new DelegatingPasswordEncoder("bcrypt",
				Map.of("bcrypt", new BCryptPasswordEncoder(), "noop", NoOpPasswordEncoder.getInstance()));
	}

}
