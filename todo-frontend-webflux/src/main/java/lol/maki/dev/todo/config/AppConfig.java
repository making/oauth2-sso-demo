package lol.maki.dev.todo.config;

import lol.maki.dev.todo.todo.TodoClient;
import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration(proxyBeanMethods = false)
@ImportHttpServices(group = "todo", types = TodoClient.class, clientType = HttpServiceGroup.ClientType.WEB_CLIENT)
public class AppConfig {

	@Bean
	public WebClientCustomizer webClientCustomizer(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
		ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
				authorizedClientManager);
		oauth.setDefaultOAuth2AuthorizedClient(true);
		return webClientBuilder -> webClientBuilder.filter(oauth);
	}

}
