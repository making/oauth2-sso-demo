package lol.maki.dev.todo;

import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping(path = "/todos")
public class TodoController {

	private final TodoService todoService;

	public TodoController(TodoService todoService) {
		this.todoService = todoService;
	}

	@GetMapping(path = "")
	public ResponseEntity<List<Todo>> getTodos() {
		List<Todo> todos = this.todoService.getTodos();
		return ResponseEntity.ok(todos);
	}

	@GetMapping(path = "/{todoId}")
	public ResponseEntity<Todo> getTodo(@PathVariable("todoId") UUID todoId) {
		Todo todo = this.todoService.getTodo(todoId);
		return ResponseEntity.ok(todo);
	}

	@PostMapping(path = "")
	public ResponseEntity<Todo> postTodos(@RequestBody Map<String, Object> request, @AuthenticationPrincipal Jwt jwt,
			UriComponentsBuilder builder) {
		String email = jwt.getClaimAsString("email");
		Todo created = this.todoService.create((String) request.get("todoTitle"), email);
		URI uri = builder.pathSegment("todos", created.todoId().toString()).build().toUri();
		return ResponseEntity.created(uri).body(created);
	}

	@PatchMapping(path = "/{todoId}")
	public ResponseEntity<Todo> patchTodo(@PathVariable("todoId") UUID todoId, @RequestBody Map<String, Object> request,
			@AuthenticationPrincipal Jwt jwt) {
		String email = jwt.getClaimAsString("email");
		Todo updated = this.todoService.update(todoId, (String) request.get("todoTitle"),
				(Boolean) request.get("finished"), email);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping(path = "/{todoId}")
	public ResponseEntity<Void> deleteTodo(@PathVariable("todoId") UUID todoId) {
		this.todoService.deleteById(todoId);
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(TodoService.NotFoundException.class)
	public ResponseEntity<?> handleNotFound(TodoService.NotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
	}

	@ExceptionHandler(ConstraintViolationsException.class)
	public ResponseEntity<?> handleConstraintViolations(ConstraintViolationsException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(Map.of("message", "Validation failed", "violations",
					ConstraintViolations.of(e.violations()).details()));
	}

}
