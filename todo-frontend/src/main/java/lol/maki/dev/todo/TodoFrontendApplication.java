package lol.maki.dev.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TodoFrontendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoFrontendApplication.class, args);
	}

}
