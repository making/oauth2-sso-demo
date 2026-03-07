package lol.maki.dev.authorization;

import java.security.Principal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PasskeyController {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
		.withZone(ZoneId.systemDefault());

	private final UserCredentialRepository credentialsRepository;

	private final PublicKeyCredentialUserEntityRepository publicKeyUserRepository;

	public PasskeyController(UserCredentialRepository credentialsRepository,
			PublicKeyCredentialUserEntityRepository publicKeyUserRepository) {
		this.credentialsRepository = credentialsRepository;
		this.publicKeyUserRepository = publicKeyUserRepository;
	}

	@GetMapping(path = "/passkeys")
	String passkeys(Principal principal, Model model) {
		PublicKeyCredentialUserEntity userEntity = this.publicKeyUserRepository.findByUsername(principal.getName());
		if (userEntity == null) {
			model.addAttribute("passkeys", List.of());
		}
		else {
			List<CredentialRecord> records = this.credentialsRepository.findByUserId(userEntity.getId());
			List<Map<String, String>> passkeys = records.stream().map(record -> {
				Instant created = record.getCreated();
				Instant lastUsed = record.getLastUsed();
				return Map.of("credentialId", record.getCredentialId().toBase64UrlString(), "label", record.getLabel(),
						"created", FORMATTER.format(created), "lastUsed", FORMATTER.format(lastUsed));
			}).toList();
			model.addAttribute("passkeys", passkeys);
		}
		return "passkeys";
	}

}
