package lol.maki.dev.todo.todo.web;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import lol.maki.dev.todo.todo.Todo;
import lol.maki.dev.todo.todo.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
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

	@InitBinder("todoCreateRequest")
	public void initCreateBinder(WebDataBinder binder) {
		binder.addValidators(todoCreateRequestValidator);
	}

	@InitBinder("todoUpdateRequest")
	public void initUpdateBinder(WebDataBinder binder) {
		binder.addValidators(todoUpdateRequestValidator);
	}

	@GetMapping(path = "")
	public ResponseEntity<List<Todo>> getTodos() {
		List<Todo> todos = this.todoService.getTodos();
		return ResponseEntity.ok(todos);
	}

	@GetMapping(path = "/{todoId}")
	public ResponseEntity<Todo> getTodo(@PathVariable UUID todoId) {
		Todo todo = this.todoService.getTodo(todoId);
		return ResponseEntity.ok(todo);
	}

	@PostMapping(path = "")
	public ResponseEntity<?> postTodos(@RequestBody @Validated TodoCreateRequest request,
			@AuthenticationPrincipal Jwt jwt, UriComponentsBuilder builder) {
		String email = jwt.getClaimAsString("email");
		Todo created = this.todoService.create(request.todoTitle(), email);
		URI uri = builder.pathSegment("todos", created.todoId().toString()).build().toUri();
		return ResponseEntity.created(uri).body(created);
	}

	@PatchMapping(path = "/{todoId}")
	public ResponseEntity<?> patchTodo(@PathVariable UUID todoId, @RequestBody @Validated TodoUpdateRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		String email = jwt.getClaimAsString("email");
		Todo todo = this.todoService.getTodo(todoId);
		boolean finished = request.finished() != null ? request.finished() : todo.finished();
		Todo updated = this.todoService.update(todoId, request.todoTitle(), finished, email);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping(path = "/{todoId}")
	public ResponseEntity<Void> deleteTodo(@PathVariable UUID todoId) {
		this.todoService.deleteById(todoId);
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(TodoService.NotFoundException.class)
	public ResponseEntity<?> handleNotFound(TodoService.NotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(Map.of("message", Objects.toString(e.getMessage(), "Not found")));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
		List<Map<String, String>> violations = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.filter(error -> error.getDefaultMessage() != null)
			.map(error -> Map.of("defaultMessage", error.getDefaultMessage()))
			.toList();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(Map.of("message", "Validation failed", "violations", violations));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(Map.of("message", "Validation failed", "violations",
					List.of(Map.of("defaultMessage", "Request body is not readable"))));
	}

	public record TodoCreateRequest(String todoTitle) {
	}

	public record TodoUpdateRequest(String todoTitle, Boolean finished) {
	}

	static void validateTodoTitle(String todoTitle, Errors errors) {
		if (todoTitle.isBlank()) {
			errors.rejectValue("todoTitle", "notBlank", "\"todoTitle\" must not be blank");
		}
		else if (todoTitle.length() > 255) {
			errors.rejectValue("todoTitle", "maxLength",
					"The size of \"todoTitle\" must be less than or equal to 255. The given size is "
							+ todoTitle.length());
		}
	}

	static final Validator todoCreateRequestValidator = Validator.forType(TodoCreateRequest.class,
			(request, errors) -> {
				if (request.todoTitle() == null) {
					errors.rejectValue("todoTitle", "notBlank", "\"todoTitle\" must not be blank");
				}
				else {
					validateTodoTitle(request.todoTitle(), errors);
				}
			});

	static final Validator todoUpdateRequestValidator = Validator.forType(TodoUpdateRequest.class,
			(request, errors) -> {
				if (request.todoTitle() != null) {
					validateTodoTitle(request.todoTitle(), errors);
				}
			});

}
