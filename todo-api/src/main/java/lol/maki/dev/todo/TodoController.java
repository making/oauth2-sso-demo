package lol.maki.dev.todo;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.IdGenerator;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping(path = "/todos")
public class TodoController {

	private final TodoRepository todoRepository;

	private final IdGenerator idGenerator;

	private final Clock clock;

	public TodoController(TodoRepository todoRepository, IdGenerator idGenerator, Clock clock) {
		this.todoRepository = todoRepository;
		this.idGenerator = idGenerator;
		this.clock = clock;
	}

	@GetMapping(path = "")
	public ResponseEntity<List<Todo>> getTodos() {
		List<Todo> todos = this.todoRepository.findAll();
		return ResponseEntity.ok(todos);
	}

	@GetMapping(path = "/{todoId}")
	public ResponseEntity<Todo> getTodo(@PathVariable("todoId") String todoId) {
		Todo todo = this.todoRepository.findById(todoId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Todo not found: todoId=%s".formatted(todoId)));
		return ResponseEntity.ok(todo);
	}

	@PostMapping(path = "")
	public ResponseEntity<Todo> postTodos(@RequestBody Todo todo, @AuthenticationPrincipal Jwt jwt,
			UriComponentsBuilder builder) {
		Instant now = this.clock.instant();
		String email = jwt.getClaimAsString("email");
		Todo initialized = TodoBuilder.from(todo)
			.todoId(this.idGenerator.generateId().toString())
			.createdBy(email)
			.createdAt(now)
			.updatedBy(email)
			.updatedAt(now)
			.build();
		Todo created = this.todoRepository.save(initialized);
		URI uri = builder.pathSegment("todos", created.todoId()).build().toUri();
		return ResponseEntity.created(uri).body(created);
	}

	@PatchMapping(path = "/{todoId}")
	public ResponseEntity<Todo> patchTodo(@PathVariable("todoId") String todoId, @RequestBody Todo todo,
			@AuthenticationPrincipal Jwt jwt) {
		Optional<Todo> updated = this.todoRepository.findById(todoId).map(t -> {
			TodoBuilder builder = TodoBuilder.from(t);
			boolean touched = false;
			if (todo.todoTitle() != null) {
				builder.todoTitle(todo.todoTitle());
				touched = true;
			}
			if (!Objects.equals(todo.finished(), t.finished())) {
				builder.finished(todo.finished());
				touched = true;
			}
			if (touched) {
				builder.updatedBy(jwt.getClaimAsString("email"));
				builder.updatedAt(this.clock.instant());
			}
			return builder.build();
		}).map(this.todoRepository::save);
		return ResponseEntity.of(updated);
	}

	@DeleteMapping(path = "/{todoId}")
	public ResponseEntity<Void> deleteTodo(@PathVariable("todoId") String todoId) {
		this.todoRepository.deleteById(todoId);
		return ResponseEntity.noContent().build();
	}

}
