package lol.maki.dev.authorization;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "back-channel-logout")
public record BackChannelLogoutProps(String issuer, Map<String, String> uris) {

	public BackChannelLogoutProps {
		if (uris == null) {
			uris = Map.of();
		}
	}

}
