package lol.maki.dev.authorization;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ConsentController {

	private final RegisteredClientRepository registeredClientRepository;

	public ConsentController(RegisteredClientRepository registeredClientRepository) {
		this.registeredClientRepository = registeredClientRepository;
	}

	@GetMapping(path = "/oauth2/consent")
	public String consent(Model model, Principal principal,
			@RequestParam(name = OAuth2ParameterNames.SCOPE) String scope,
			@RequestParam(name = OAuth2ParameterNames.CLIENT_ID) String clientId,
			@RequestParam(name = OAuth2ParameterNames.STATE) String state) {
		RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
		if (registeredClient == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Cannot find RegisteredClient with id: " + clientId);
		}
		Set<String> requestedScopes = new LinkedHashSet<>(
				Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
		Set<String> supportedScopes = registeredClient.getScopes();
		Set<String> scopesToAuthorize = requestedScopes.stream()
			.filter(supportedScopes::contains)
			.filter(s -> !s.equals(OidcScopes.OPENID))
			.collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
		model.addAttribute("clientId", clientId);
		model.addAttribute("clientName", registeredClient.getClientName());
		model.addAttribute("state", state);
		model.addAttribute("scopes", scopesToAuthorize);
		model.addAttribute("principalName", principal.getName());
		return "consent";
	}

}
