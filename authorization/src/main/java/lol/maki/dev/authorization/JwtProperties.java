package lol.maki.dev.authorization;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(Map<String, KeySet> keys) {

	public record KeySet(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
	}
}