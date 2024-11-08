package lol.maki.dev.todo;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;

@RestController
@RequestMapping(path = "/api/todos")
public class TodoController implements TodoClient {

	private final TodoClient delegate;

	public TodoController(TodoClient delegate) {
		this.delegate = delegate;
	}

	@Override
	public List<Todo> listTodos() {
		return this.delegate.listTodos();
	}

	@Override
	public Todo getTodo(String todoId) {
		return this.delegate.getTodo(todoId);
	}

	@Override
	public Todo postTodo(Todo todo) {
		return this.delegate.postTodo(todo);
	}

	@Override
	public Todo patchTodo(String todoId, Todo todo) {
		return this.delegate.patchTodo(todoId, todo);
	}

	@Override
	public void deleteTodo(String todoId) {
		this.delegate.deleteTodo(todoId);
	}

	@ExceptionHandler(HttpStatusCodeException.class)
	public ResponseEntity<?> handleHttpStatusCodeException(HttpStatusCodeException e) {
		return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
	}

}
