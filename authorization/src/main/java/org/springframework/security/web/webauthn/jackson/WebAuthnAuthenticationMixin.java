package org.springframework.security.web.webauthn.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthentication;

import java.util.Collection;

/**
 * Jackson mixin for {@link WebAuthnAuthentication}
 *
 * @author Toshiaki Maki
 * @since 7.1
 */
@JsonIgnoreProperties({ "authenticated" })
abstract class WebAuthnAuthenticationMixin {

	WebAuthnAuthenticationMixin(@JsonProperty("principal") PublicKeyCredentialUserEntity principal,
			@JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities) {
	}

}
