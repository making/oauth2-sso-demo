package lol.maki.dev.todo;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WhoamiController {

	@GetMapping(path = "/whoami")
	public Map<String, String> whoami(@AuthenticationPrincipal OidcUser user, CsrfToken csrfToken) {
		return Map.of("name", user.getName(), "email", user.getEmail(), "csrfToken", csrfToken.getToken());
	}

}
