package lol.maki.dev.config;

import lol.maki.dev.jwt.IdTokenEnhancer;
import lol.maki.dev.jwt.JwtClamsEnhancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.security.KeyPair;
import java.util.List;

/**
 * https://docs.spring.io/spring-security-oauth2-boot/docs/2.3.x-SNAPSHOT/reference/html5/#oauth2-boot-authorization-server-spring-security-oauth2-resource-server
 */
@EnableAuthorizationServer
@Configuration
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    private final OauthProperties oauthProperties;
    private final AuthenticationManager authenticationManager;
    private final KeyPair keyPair;
    private final JwtClamsEnhancer jwtClamsEnhancer;

    public AuthorizationServerConfig(OauthProperties oauthProperties, AuthenticationConfiguration authenticationConfiguration, JwtProperties props, JwtClamsEnhancer jwtClamsEnhancer) throws Exception {
        this.oauthProperties = oauthProperties;
        this.authenticationManager = authenticationConfiguration.getAuthenticationManager();
        this.keyPair = props.getKeyPair();
        this.jwtClamsEnhancer = jwtClamsEnhancer;
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        final TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(List.of(this.jwtClamsEnhancer, jwtAccessTokenConverter(), new IdTokenEnhancer(jwtAccessTokenConverter())));
        endpoints
                .authenticationManager(this.authenticationManager)
                .tokenEnhancer(tokenEnhancerChain)
                .tokenStore(tokenStore());
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientId -> {
            final ClientDetails clientDetails = this.oauthProperties.getClients().get(clientId);
            if (clientDetails == null) {
                throw new ClientRegistrationException(String.format("%s is not registered.", clientId));
            }
            return clientDetails;
        });
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        final JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(this.keyPair);
        return converter;
    }

}
