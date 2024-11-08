package lol.maki.dev.authorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AuthorizationApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthorizationApplication.class, args);
	}

}
