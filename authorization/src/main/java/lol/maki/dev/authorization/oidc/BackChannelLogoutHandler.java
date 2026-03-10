package lol.maki.dev.authorization.oidc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jspecify.annotations.Nullable;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * A custom {@link LogoutHandler} that sends OIDC Back-Channel Logout Tokens to registered
 * clients when a user logs out from the Authorization Server.
 *
 * <p>
 * Spring Authorization Server does not natively support sending Back-Channel Logout
 * Tokens. While the client side (Spring Security OAuth2 Client) provides built-in support
 * for <em>receiving</em> and validating logout tokens via
 * {@code .oidcLogout(logout -> logout.backChannel(...))}, the server side has no
 * corresponding mechanism to <em>send</em> them. This class fills that gap by generating
 * a signed JWT Logout Token conforming to the
 * <a href="https://openid.net/specs/openid-connect-backchannel-1_0.html">OIDC
 * Back-Channel Logout</a> specification and POSTing it to each registered client's
 * back-channel logout URI.
 *
 * @see <a href=
 * "https://openid.net/specs/openid-connect-backchannel-1_0.html#LogoutToken">OIDC
 * Back-Channel Logout Token</a>
 */
public class BackChannelLogoutHandler implements LogoutHandler {

	private static final String BACK_CHANNEL_LOGOUT_EVENT = "http://schemas.openid.net/event/backchannel-logout";

	private final Log logger = LogFactory.getLog(getClass());

	private final JwtEncoder jwtEncoder;

	private final RestClient restClient;

	private final String issuer;

	private final Map<String, String> backChannelLogoutUris;

	/**
	 * @param jwkSource the JWK source for signing logout tokens
	 * @param issuer the issuer URI of the Authorization Server
	 * @param backChannelLogoutUris a map of client_id to back-channel logout URI
	 */
	public BackChannelLogoutHandler(JWKSource<SecurityContext> jwkSource, RestClient.Builder restClientBuilder,
			String issuer, Map<String, String> backChannelLogoutUris) {
		this.jwtEncoder = new NimbusJwtEncoder(jwkSource);
		this.restClient = restClientBuilder.build();
		this.issuer = issuer;
		this.backChannelLogoutUris = backChannelLogoutUris;
	}

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response,
			@Nullable Authentication authentication) {
		if (authentication == null) {
			return;
		}
		String subject = authentication.getName();
		for (Map.Entry<String, String> entry : this.backChannelLogoutUris.entrySet()) {
			String clientId = entry.getKey();
			String logoutUri = entry.getValue();
			try {
				String logoutToken = createLogoutToken(subject, clientId);
				sendLogoutToken(logoutUri, logoutToken);
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("Sent back-channel logout token to client: " + clientId);
				}
			}
			catch (Exception ex) {
				this.logger.warn("Failed to send back-channel logout token to client: " + clientId, ex);
			}
		}
	}

	private String createLogoutToken(String subject, String clientId) {
		Instant now = Instant.now();
		JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256).build();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(this.issuer)
			.subject(subject)
			.audience(List.of(clientId))
			.issuedAt(now)
			.id(UUID.randomUUID().toString())
			.claim("events", Map.of(BACK_CHANNEL_LOGOUT_EVENT, Map.of()))
			.build();
		return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
	}

	private void sendLogoutToken(String logoutUri, String logoutToken) {
		this.restClient.post()
			.uri(logoutUri)
			.body(MultiValueMap.fromSingleValue(Map.of("logout_token", logoutToken)))
			.retrieve()
			.toBodilessEntity();
	}

}
