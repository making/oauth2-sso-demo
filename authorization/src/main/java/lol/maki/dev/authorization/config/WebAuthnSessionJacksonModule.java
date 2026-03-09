package lol.maki.dev.authorization.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.node.MissingNode;

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
 * entries and a custom deserializer.
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
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
			isGetterVisibility = JsonAutoDetect.Visibility.NONE)
	@JsonDeserialize(using = WebAuthnAuthenticationDeserializer.class)
	abstract static class WebAuthnAuthenticationMixin {

	}

	static class WebAuthnAuthenticationDeserializer extends ValueDeserializer<WebAuthnAuthentication> {

		@Override
		public WebAuthnAuthentication deserialize(JsonParser jp, DeserializationContext ctxt) throws JacksonException {
			JsonNode jsonNode = ctxt.readTree(jp);
			JsonNode principalNode = readJsonNode(jsonNode, "principal");
			PublicKeyCredentialUserEntity principal = deserializePrincipal(principalNode);
			JsonNode authoritiesNode = readJsonNode(jsonNode, "authorities");
			List<GrantedAuthority> authorities = ctxt.readTreeAsValue(authoritiesNode,
					ctxt.getTypeFactory().constructType(new TypeReference<>() {
					}));
			WebAuthnAuthentication token = new WebAuthnAuthentication(principal, authorities);
			JsonNode detailsNode = readJsonNode(jsonNode, "details");
			if (!detailsNode.isNull() && !detailsNode.isMissingNode()) {
				Object details = ctxt.readTreeAsValue(detailsNode, Object.class);
				token.setDetails(details);
			}
			return token;
		}

		private PublicKeyCredentialUserEntity deserializePrincipal(JsonNode principalNode) {
			String name = readJsonNode(principalNode, "name").asString();
			String idBase64 = readJsonNode(principalNode, "id").asString();
			Bytes id = Bytes.fromBase64(idBase64);
			JsonNode displayNameNode = readJsonNode(principalNode, "displayName");
			String displayName = displayNameNode.isNull() || displayNameNode.isMissingNode() ? null
					: displayNameNode.asString();
			return ImmutablePublicKeyCredentialUserEntity.builder().name(name).id(id).displayName(displayName).build();
		}

		private JsonNode readJsonNode(JsonNode jsonNode, String field) {
			return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
		}

	}

}
