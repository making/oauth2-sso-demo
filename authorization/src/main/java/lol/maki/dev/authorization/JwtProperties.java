package lol.maki.dev.authorization;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(List<KeySet> keySets) {

	public record KeySet(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
	}
}