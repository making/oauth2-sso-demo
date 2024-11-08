package lol.maki.dev.todo;

import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Configuration(proxyBeanMethods = false)
public class AppConfig {

	@Bean
	public RestClientCustomizer restClientCustomizer(OAuth2AuthorizedClientManager authorizedClientManager,
			LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor, TodoProps props) {
		return builder -> builder.requestInterceptor(logbookClientHttpRequestInterceptor)
			.requestInterceptor(new OAuth2ClientHttpRequestInterceptor(authorizedClientManager))
			.defaultRequest(req -> req.attributes(clientRegistrationId("todo-frontend")))
			.baseUrl(props.apiUrl());
	}

	@Bean
	public TodoClient todoClient(RestClient.Builder restClientBuilder) {
		RestClientAdapter adapter = RestClientAdapter.create(restClientBuilder.build());
		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
		return factory.createClient(TodoClient.class);
	}

}
