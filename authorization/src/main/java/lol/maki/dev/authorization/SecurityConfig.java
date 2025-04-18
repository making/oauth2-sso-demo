package lol.maki.dev.authorization;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.util.List;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

	/**
	 * See <a href=
	 * "https://docs.spring.io/spring-authorization-server/reference/getting-started.html#defining-required-components">Reference</a>
	 */
	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = authorizationServer();
		http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer,
					(authorizationServer) -> authorizationServer.oidc(Customizer.withDefaults()))
			.authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
			.exceptionHandling((exceptions) -> exceptions.defaultAuthenticationEntryPointFor(
					new LoginUrlAuthenticationEntryPoint("/login"), new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((authorize) -> authorize.requestMatchers(EndpointRequest.toAnyEndpoint())
			.permitAll()
			.requestMatchers("/login", "/error")
			.permitAll()
			.anyRequest()
			.authenticated()).formLogin(form -> form.loginPage("/login"));
		return http.build();
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource(JwtProperties props) {
		List<JWK> keys = props.keys()
			.entrySet()
			.stream()
			.map(entry -> (JWK) new RSAKey.Builder(entry.getValue().publicKey())
				.privateKey(entry.getValue().privateKey())
				.keyID(entry.getKey())
				.build())
			.toList();
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

}
