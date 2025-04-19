package lol.maki.dev.todo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/todos")
public class TodoController implements TodoClient {

	private final TodoClient delegate;

	public TodoController(TodoClient delegate) {
		this.delegate = delegate;
	}

	@Override
	public Flux<Todo> listTodos() {
		return this.delegate.listTodos();
	}

	@Override
	public Mono<Todo> getTodo(String todoId) {
		return this.delegate.getTodo(todoId);
	}

	@Override
	public Mono<Todo> postTodo(Todo todo) {
		return this.delegate.postTodo(todo);
	}

	@Override
	public Mono<Todo> patchTodo(String todoId, Todo todo) {
		return this.delegate.patchTodo(todoId, todo);
	}

	@Override
	public Mono<Void> deleteTodo(String todoId) {
		return this.delegate.deleteTodo(todoId);
	}

	@ExceptionHandler(WebClientResponseException.class)
	public ResponseEntity<?> handleWebClientResponseException(WebClientResponseException e) {
		return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
	}

}
