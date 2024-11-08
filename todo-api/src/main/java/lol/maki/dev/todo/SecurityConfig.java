package lol.maki.dev.todo;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.JwkSetUriJwtDecoderBuilderCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests(authorize -> authorize.requestMatchers(EndpointRequest.toAnyEndpoint())
				.permitAll()
				.requestMatchers(HttpMethod.GET, "/todos/**")
				.hasAnyAuthority("SCOPE_todo:read")
				.requestMatchers(HttpMethod.POST, "/todos/**")
				.hasAnyAuthority("SCOPE_todo:write")
				.requestMatchers(HttpMethod.PATCH, "/todos/**")
				.hasAnyAuthority("SCOPE_todo:write")
				.requestMatchers(HttpMethod.DELETE, "/todos/**")
				.hasAnyAuthority("SCOPE_todo:write")
				.anyRequest()
				.authenticated())
			.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
			.csrf(csrf -> csrf.ignoringRequestMatchers("/todos/**"))
			.build();
	}

	@Bean
	public JwkSetUriJwtDecoderBuilderCustomizer jwkSetUriJwtDecoderBuilderCustomizer(
			RestTemplateBuilder restTemplateBuilder) {
		return builder -> builder.restOperations(restTemplateBuilder.build());
	}

}
