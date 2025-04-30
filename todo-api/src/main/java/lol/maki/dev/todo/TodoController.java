package lol.maki.dev.todo;

import am.ik.yavi.arguments.Arguments;
import am.ik.yavi.arguments.Arguments1Validator;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validated;
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
	public ResponseEntity<?> postTodos(@RequestBody Map<String, String> request, @AuthenticationPrincipal Jwt jwt,
			UriComponentsBuilder builder) {
		return TodoCreateRequest.parse(request).fold(this::badRequest, req -> {
			String email = jwt.getClaimAsString("email");
			Todo created = this.todoService.create(req.todoTitle(), email);
			URI uri = builder.pathSegment("todos", created.todoId().toString()).build().toUri();
			return ResponseEntity.created(uri).body(created);
		});
	}

	@PatchMapping(path = "/{todoId}")
	public ResponseEntity<?> patchTodo(@PathVariable("todoId") UUID todoId, @RequestBody Map<String, String> request,
			@AuthenticationPrincipal Jwt jwt) {
		return TodoUpdateRequest.parse(request).fold(this::badRequest, req -> {
			String email = jwt.getClaimAsString("email");
			Todo updated = this.todoService.update(todoId, req.todoTitle(), req.finished(), email);
			return ResponseEntity.ok(updated);
		});
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

	ResponseEntity<Map<String, Object>> badRequest(List<ConstraintViolation> violations) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(Map.of("message", "Validation failed", "violations", ConstraintViolations.of(violations).details()));
	}

	record TodoCreateRequest(String todoTitle) {
		private static final Arguments1Validator<Map<String, String>, TodoCreateRequest> validator = Todo.todoTitleValidator
			.andThen(TodoCreateRequest::new)
			.compose(map -> map.get("todoTitle"));

		static Validated<TodoCreateRequest> parse(Map<String, String> map) {
			return validator.validate(map);
		}
	}

	record TodoUpdateRequest(String todoTitle, Boolean finished) {
		private static final Arguments1Validator<Map<String, String>, TodoUpdateRequest> validator = Todo.todoTitleValidator
			.split(StringValidatorBuilder.of("finished", c -> c.notNull().oneOf(List.of("true", "false")))
				.build(Boolean::parseBoolean)
				.andThen(Todo.finishedValidator))
			.apply(TodoUpdateRequest::new)
			.compose(map -> Arguments.of(map.get("todoTitle"), map.get("finished")));

		static Validated<TodoUpdateRequest> parse(Map<String, String> map) {
			return validator.validate(map);
		}
	}

}
