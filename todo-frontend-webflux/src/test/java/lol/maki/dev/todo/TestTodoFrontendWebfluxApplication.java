package lol.maki.dev.todo;

import org.springframework.boot.SpringApplication;

public class TestTodoFrontendWebfluxApplication {

	public static void main(String[] args) {
		SpringApplication.from(TodoFrontendWebfluxApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
