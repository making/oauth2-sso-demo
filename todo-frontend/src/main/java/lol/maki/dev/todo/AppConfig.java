package lol.maki.dev.todo;

import org.springframework.web.service.registry.ImportHttpServices;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Configuration(proxyBeanMethods = false)
@ImportHttpServices(group = "todo", types = TodoClient.class)
public class AppConfig {

	@Bean
	public RestClientCustomizer restClientCustomizer(OAuth2AuthorizedClientManager authorizedClientManager,
			LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor) {
		return builder -> builder.requestInterceptor(logbookClientHttpRequestInterceptor)
			.requestInterceptor(new OAuth2ClientHttpRequestInterceptor(authorizedClientManager))
			.defaultRequest(req -> req.attributes(clientRegistrationId("todo-frontend")));
	}

}
