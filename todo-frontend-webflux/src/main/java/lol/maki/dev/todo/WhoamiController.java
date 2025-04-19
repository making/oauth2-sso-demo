package lol.maki.dev.todo;

import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class WhoamiController {

	@GetMapping(path = "/whoami")
	public Mono<Map<String, String>> whoami(@AuthenticationPrincipal OidcUser user, ServerWebExchange exchange) {
		Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
		if (csrfToken == null) {
			return Mono.just(Map.of("name", user.getName(), "email", user.getEmail(), "csrfToken", ""));
		}
		return csrfToken
			.map(token -> Map.of("name", user.getName(), "email", user.getEmail(), "csrfToken", token.getToken()));
	}

}
