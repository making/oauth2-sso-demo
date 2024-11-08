package lol.maki.dev.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TodoUiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoUiApplication.class, args);
	}

}
