package lol.maki.dev.authorization.config;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.core.Version;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.jackson.SecurityJacksonModule;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthentication;

/**
 * Jackson module that enables serialization and deserialization of
 * {@link WebAuthnAuthentication} for session persistence (e.g.,
 * {@link org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService}).
 *
 * <p>
 * The built-in {@code WebauthnJacksonModule} handles WebAuthn API types ({@code Bytes},
 * {@code PublicKeyCredential}, etc.) but does not cover {@link WebAuthnAuthentication},
 * which is an authentication token stored as the principal in OAuth2 authorization
 * attributes. This module fills that gap by registering the necessary type validator
 * entries and mixins for {@link WebAuthnAuthentication} and
 * {@link ImmutablePublicKeyCredentialUserEntity}.
 */
@SuppressWarnings("serial")
class WebAuthnSessionJacksonModule extends SecurityJacksonModule {

	WebAuthnSessionJacksonModule() {
		super(WebAuthnSessionJacksonModule.class.getName(), new Version(1, 0, 0, null, null, null));
	}

	@Override
	public void configurePolymorphicTypeValidator(BasicPolymorphicTypeValidator.Builder builder) {
		builder.allowIfSubType(WebAuthnAuthentication.class)
			.allowIfSubType(ImmutablePublicKeyCredentialUserEntity.class);
	}

	@Override
	public void setupModule(SetupContext context) {
		context.setMixIn(WebAuthnAuthentication.class, WebAuthnAuthenticationMixin.class);
		context.setMixIn(ImmutablePublicKeyCredentialUserEntity.class,
				ImmutablePublicKeyCredentialUserEntityMixin.class);
	}

	@JsonIgnoreProperties({ "authenticated" })
	abstract static class WebAuthnAuthenticationMixin {

		WebAuthnAuthenticationMixin(@JsonProperty("principal") PublicKeyCredentialUserEntity principal,
				@JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities) {
		}

	}

	abstract static class ImmutablePublicKeyCredentialUserEntityMixin {

		ImmutablePublicKeyCredentialUserEntityMixin(@JsonProperty("name") String name, @JsonProperty("id") Bytes id,
				@JsonProperty("displayName") String displayName) {
		}

	}

}