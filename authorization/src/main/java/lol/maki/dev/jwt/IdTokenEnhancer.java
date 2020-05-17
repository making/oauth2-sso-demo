package lol.maki.dev.jwt;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.Map;
import java.util.Set;

public class IdTokenEnhancer implements TokenEnhancer {
    private final JwtAccessTokenConverter jwtAccessTokenConverter;

    public IdTokenEnhancer(JwtAccessTokenConverter jwtAccessTokenConverter) {
        this.jwtAccessTokenConverter = jwtAccessTokenConverter;
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        if (accessToken.getScope().contains(OidcScopes.OPENID)) {
            final DefaultOAuth2AccessToken idToken = new DefaultOAuth2AccessToken(accessToken);
            idToken.setScope(Set.of(OidcScopes.OPENID));
            idToken.setRefreshToken(null);
            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(Map.of(OidcParameterNames.ID_TOKEN, this.jwtAccessTokenConverter.enhance(idToken, authentication).getValue()));
        }
        return accessToken;
    }
}
