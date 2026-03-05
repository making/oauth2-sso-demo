package lol.maki.dev.todo;

import org.springframework.security.oauth2.client.web.client.support.OAuth2RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Configuration(proxyBeanMethods = false)
@ImportHttpServices(group = "todo", types = TodoClient.class)
public class AppConfig {

	@Bean
	public RestClientCustomizer restClientCustomizer(
			LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor) {
		return builder -> builder.requestInterceptor(logbookClientHttpRequestInterceptor)
			.defaultRequest(req -> req.attributes(clientRegistrationId("todo-frontend")));
	}

	@Bean
	public OAuth2RestClientHttpServiceGroupConfigurer securityConfigurer(OAuth2AuthorizedClientManager manager) {
		return OAuth2RestClientHttpServiceGroupConfigurer.from(manager);
	}

}
