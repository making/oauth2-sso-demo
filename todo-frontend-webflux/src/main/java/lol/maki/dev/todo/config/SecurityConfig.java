package lol.maki.dev.todo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.OidcBackChannelServerLogoutHandler;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.server.session.InMemoryReactiveOidcSessionRegistry;
import org.springframework.security.oauth2.client.oidc.server.session.ReactiveOidcSessionRegistry;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
			ReactiveClientRegistrationRepository clientRegistrationRepository) {
		return http.authorizeExchange(auth -> auth.anyExchange().authenticated())
			.oauth2Login(Customizer.withDefaults())
			.oauth2Client(Customizer.withDefaults())
			.oidcLogout(logout -> logout.backChannel(Customizer.withDefaults()))
			.logout(logout -> {
				OidcClientInitiatedServerLogoutSuccessHandler logoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(
						clientRegistrationRepository);
				logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
				logout.logoutSuccessHandler(logoutSuccessHandler);
			})
			.csrf(Customizer.withDefaults())
			.build();
	}

	/**
	 * The {@link ReactiveOidcSessionRegistry} maintains the mapping between OIDC provider
	 * sessions and local WebSession IDs. This mapping is required for Back-Channel Logout
	 * to locate and invalidate the correct sessions.
	 *
	 * <p>
	 * Note: {@link InMemoryReactiveOidcSessionRegistry} is the only implementation
	 * provided by Spring Security. The mappings are lost on application restart, meaning
	 * Back-Channel Logout will not work for sessions established before the restart. In
	 * production, a persistent implementation would be needed.
	 */
	@Bean
	public ReactiveOidcSessionRegistry reactiveOidcSessionRegistry() {
		return new InMemoryReactiveOidcSessionRegistry();
	}

	@Bean
	public OidcBackChannelServerLogoutHandler oidcBackChannelServerLogoutHandler(
			ReactiveOidcSessionRegistry sessionRegistry) {
		return new OidcBackChannelServerLogoutHandler(sessionRegistry);
	}

}
