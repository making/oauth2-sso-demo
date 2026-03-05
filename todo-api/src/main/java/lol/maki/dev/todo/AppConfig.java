package lol.maki.dev.todo;

import java.time.InstantSource;

import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import org.springframework.boot.restclient.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.IdGenerator;
import org.springframework.util.JdkIdGenerator;

@Configuration(proxyBeanMethods = false)
public class AppConfig {

	@Bean
	public InstantSource instantSource() {
		return InstantSource.system();
	}

	@Bean
	public IdGenerator idGenerator() {
		return new JdkIdGenerator();
	}

	@Bean
	public RestTemplateCustomizer restTemplateCustomizer(
			LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor) {
		return restTemplate -> restTemplate.getInterceptors().addFirst(logbookClientHttpRequestInterceptor);
	}

}
