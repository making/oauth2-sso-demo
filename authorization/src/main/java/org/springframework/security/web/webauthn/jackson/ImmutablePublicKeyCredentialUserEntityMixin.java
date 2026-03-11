package org.springframework.security.web.webauthn.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;

/**
 * Jackson mixin for {@link ImmutablePublicKeyCredentialUserEntity}
 *
 * @author Toshiaki Maki
 * @since 7.1
 */
abstract class ImmutablePublicKeyCredentialUserEntityMixin {

	ImmutablePublicKeyCredentialUserEntityMixin(@JsonProperty("name") String name, @JsonProperty("id") Bytes id,
			@JsonProperty("displayName") String displayName) {
	}

}
