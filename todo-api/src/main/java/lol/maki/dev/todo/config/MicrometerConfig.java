package lol.maki.dev.todo.config;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MicrometerConfig {

	@Bean
	public MeterFilter meterFilter() {
		return MeterFilter.deny(id -> {
			String uri = id.getTag("uri");
			if (uri == null) {
				uri = id.getTag("url.path");
			}
			return uri != null && (uri.equals("/readyz") || uri.equals("/livez") || uri.startsWith("/actuator")
					|| uri.startsWith("/_static"));
		});
	}

}