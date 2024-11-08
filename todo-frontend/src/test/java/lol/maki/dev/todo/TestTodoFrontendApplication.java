package lol.maki.dev.todo;

import org.springframework.boot.SpringApplication;

public class TestTodoFrontendApplication {

	public static void main(String[] args) {
		SpringApplication.from(TodoFrontendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
