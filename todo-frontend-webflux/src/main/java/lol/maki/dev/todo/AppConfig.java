package lol.maki.dev.todo;

import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration(proxyBeanMethods = false)
public class AppConfig {

	@Bean
	public WebClientCustomizer webClientCustomizer(ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
			TodoProps props) {
		ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
				authorizedClientManager);
		oauth.setDefaultOAuth2AuthorizedClient(true);
		return webClientBuilder -> webClientBuilder.filter(oauth).baseUrl(props.apiUrl());
	}

	@Bean
	public TodoClient todoClient(WebClient.Builder webClientBuilder) {
		WebClientAdapter adapter = WebClientAdapter.create(webClientBuilder.build());
		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
		return factory.createClient(TodoClient.class);
	}

}
