package lol.maki.dev.wellknown;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lol.maki.dev.config.JwtProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;


@RestController
public class WellKnownEndpoint {
    private final KeyPair keyPair;

    public WellKnownEndpoint(JwtProperties props) {
        this.keyPair = props.getKeyPair();
    }

    /**
     * https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfig
     */
    @GetMapping(path = {".well-known/openid-configuration", "oauth/token/.well-known/openid-configuration"})
    public Map<String, Object> openIdConfiguration(UriComponentsBuilder builder) {
        return Map.of("issuer", builder.replacePath("oauth/token").build().toString(),
                "authorization_endpoint", builder.replacePath("oauth/authorize").build().toString(),
                "token_endpoint", builder.replacePath("oauth/token").build().toString(),
                "jwks_uri", builder.replacePath("token_keys").build().toString(),
                "subject_types_supported", List.of("public"));
    }

    /**
     * https://docs.spring.io/spring-security-oauth2-boot/docs/2.3.x-SNAPSHOT/reference/html5/#oauth2-boot-authorization-server-spring-security-oauth2-resource-server-jwk-set-uri
     */
    @GetMapping(path = "token_keys")
    public Map<String, Object> tokenKeys() {
        final RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();
        final RSAKey key = new RSAKey.Builder(publicKey).build();
        return new JWKSet(key).toJSONObject();
    }
}
