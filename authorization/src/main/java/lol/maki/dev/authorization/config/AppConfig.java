package lol.maki.dev.authorization.config;

import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@Configuration(proxyBeanMethods = false)
public class AppConfig {

	@Bean
	RestClientCustomizer restClientCustomizer(LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor) {
		return restClient -> restClient.requestInterceptor(logbookClientHttpRequestInterceptor);
	}

}
