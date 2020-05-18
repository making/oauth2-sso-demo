package lol.maki.dev.config;

import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Configuration
public class SecurityConfig {
    private final URI authorizationServerLogoutUrl;

    public SecurityConfig(OAuth2ClientProperties clientProperties) {
        this.authorizationServerLogoutUrl = clientProperties.getProvider().values().stream().findFirst()
                .map(OAuth2ClientProperties.Provider::getIssuerUri)
                .map(UriComponentsBuilder::fromHttpUrl)
                .map(builder -> builder.replacePath("logout").build().toUri())
                .orElseThrow();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        final RedirectServerLogoutSuccessHandler logoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
        logoutSuccessHandler.setLogoutSuccessUrl(this.authorizationServerLogoutUrl);
        return http
                .authorizeExchange(exchanges -> exchanges
                        .matchers(EndpointRequest.to("health", "info", "prometheus")).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessHandler(logoutSuccessHandler))
                .csrf(csrf -> csrf.disable() /* TODO */)
                .build();
    }
}
