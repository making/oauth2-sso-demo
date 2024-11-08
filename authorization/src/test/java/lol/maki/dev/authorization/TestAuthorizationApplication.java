package lol.maki.dev.authorization;

import org.springframework.boot.SpringApplication;

public class TestAuthorizationApplication {

	public static void main(String[] args) {
		SpringApplication.from(AuthorizationApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
