package lol.maki.dev.todo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http.authorizeExchange(auth -> auth.anyExchange().authenticated())
			.oauth2Login(Customizer.withDefaults())
			.oauth2Client(Customizer.withDefaults())
			.csrf(Customizer.withDefaults())
			.build();
	}

}
