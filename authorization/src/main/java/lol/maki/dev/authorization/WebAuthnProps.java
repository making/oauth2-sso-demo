package lol.maki.dev.authorization;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "webauthn")
public record WebAuthnProps(String rpName, String rpId, Set<String> allowedOrigins) {
}
