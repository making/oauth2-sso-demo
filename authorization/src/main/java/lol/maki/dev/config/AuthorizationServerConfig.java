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
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.security.KeyPair;
import java.time.Duration;
import java.util.List;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.*;

/**
 * https://docs.spring.io/spring-security-oauth2-boot/docs/2.3.x-SNAPSHOT/reference/html5/#oauth2-boot-authorization-server-spring-security-oauth2-resource-server
 */
@EnableAuthorizationServer
@Configuration
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    private final AuthenticationManager authenticationManager;
    private final KeyPair keyPair;
    private final JwtClamsEnhancer jwtClamsEnhancer;

    public AuthorizationServerConfig(AuthenticationConfiguration authenticationConfiguration, JwtProperties props, JwtClamsEnhancer jwtClamsEnhancer) throws Exception {
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
        clients
                // `todo` client
                .inMemory()
                .withClient("todo")
                .authorizedGrantTypes(
                        AUTHORIZATION_CODE.getValue(),
                        PASSWORD.getValue(),
                        REFRESH_TOKEN.getValue())
                .secret("{noop}todo")
                .scopes("openid", "todo:read", "todo:write")
                .accessTokenValiditySeconds((int) Duration.ofDays(1).getSeconds())
                .refreshTokenValiditySeconds((int) Duration.ofDays(7).getSeconds())
                // https://docs.spring.io/spring-security/site/docs/5.3.2.RELEASE/reference/html5/#oauth2login-sample-redirect-uri
                .redirectUris("http://localhost:8080/login/oauth2/code/demo")
                .autoApprove(true)
                .and()
                // `admin` client
                .inMemory()
                .withClient("admin")
                .accessTokenValiditySeconds((int) Duration.ofHours(1).getSeconds())
                .refreshTokenValiditySeconds((int) Duration.ofHours(12).getSeconds())
                .authorizedGrantTypes(
                        CLIENT_CREDENTIALS.getValue(),
                        REFRESH_TOKEN.getValue())
                .secret("{noop}admin")
                .scopes("todo:admin");
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
