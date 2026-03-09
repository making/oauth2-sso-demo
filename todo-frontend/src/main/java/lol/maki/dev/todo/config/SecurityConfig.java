package lol.maki.dev.todo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OidcBackChannelLogoutHandler;
import org.springframework.security.oauth2.client.oidc.session.InMemoryOidcSessionRegistry;
import org.springframework.security.oauth2.client.oidc.session.OidcSessionRegistry;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
			ClientRegistrationRepository clientRegistrationRepository) {
		return http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
			.oauth2Login(Customizer.withDefaults())
			.oidcLogout(logout -> logout.backChannel(Customizer.withDefaults()))
			.logout(logout -> {
				OidcClientInitiatedLogoutSuccessHandler logoutSuccessHandler = new OidcClientInitiatedLogoutSuccessHandler(
						clientRegistrationRepository);
				logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
				logout.logoutSuccessHandler(logoutSuccessHandler);
			})
			.csrf(Customizer.withDefaults())
			.build();
	}

	/**
	 * The {@link OidcSessionRegistry} maintains the mapping between OIDC provider
	 * sessions and local HTTP sessions. This mapping is required for Back-Channel Logout
	 * to locate and invalidate the correct sessions. The same instance must be shared by
	 * {@code OidcSessionRegistryAuthenticationStrategy} (which saves the mapping on
	 * login) and {@link OidcBackChannelLogoutHandler} (which looks up the mapping on
	 * logout).
	 *
	 * <p>
	 * Note: {@link InMemoryOidcSessionRegistry} is the only implementation provided by
	 * Spring Security. The mappings are lost on application restart, meaning Back-Channel
	 * Logout will not work for sessions established before the restart (even though the
	 * HTTP sessions themselves survive in the JDBC session store). In production, a
	 * persistent implementation (e.g. JDBC-backed) would be needed.
	 */
	@Bean
	public OidcSessionRegistry oidcSessionRegistry() {
		return new InMemoryOidcSessionRegistry();
	}

	@Bean
	public OidcBackChannelLogoutHandler oidcBackChannelLogoutHandler(OidcSessionRegistry sessionRegistry) {
		OidcBackChannelLogoutHandler handler = new OidcBackChannelLogoutHandler(sessionRegistry);
		handler.setSessionCookieName("SESSION");
		return handler;
	}

	/**
	 * Disable base64 encoding for the session cookie. By default,
	 * {@link DefaultCookieSerializer} base64-encodes the session ID in the cookie value.
	 * However, Spring Security's {@link OidcBackChannelLogoutHandler} uses the raw
	 * session ID (from {@code HttpSession#getId()}) as the cookie value in its internal
	 * POST request to invalidate each session. If base64 encoding is enabled, the session
	 * repository cannot resolve the raw session ID and the session is not invalidated.
	 */
	@Bean
	public DefaultCookieSerializer cookieSerializer() {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setUseBase64Encoding(false);
		return serializer;
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

}
