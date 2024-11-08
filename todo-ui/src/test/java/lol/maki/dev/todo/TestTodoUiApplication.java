package lol.maki.dev.todo;

import org.springframework.boot.SpringApplication;

public class TestTodoUiApplication {

	public static void main(String[] args) {
		SpringApplication.from(TodoUiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
