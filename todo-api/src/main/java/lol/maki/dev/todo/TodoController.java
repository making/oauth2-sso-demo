package lol.maki.dev.todo;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("todos")
@CrossOrigin
public class TodoController {
    private final TodoRepository todoRepository;

    public TodoController(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @GetMapping(path = "")
    public ResponseEntity<List<Todo>> getTodos() {
        final List<Todo> todos = this.todoRepository.findAll();
        return ResponseEntity.ok(todos);
    }

    @GetMapping(path = "/{todoId}")
    public ResponseEntity<Todo> getTodo(@PathVariable("todoId") String todoId) {
        final Optional<Todo> todo = this.todoRepository.findById(todoId);
        return ResponseEntity.of(todo);
    }

    @PostMapping(path = "")
    public ResponseEntity<Todo> postTodos(@RequestBody Todo todo, @AuthenticationPrincipal Jwt jwt, UriComponentsBuilder builder) {
        todo.setTodoId(UUID.randomUUID().toString());
        todo.setCreatedAt(Instant.now());
        todo.setCreatedBy(jwt.getSubject());
        todo.setUpdatedAt(todo.getCreatedAt());
        todo.setUpdatedBy(todo.getCreatedBy());
        final Todo created = this.todoRepository.create(todo);
        final URI uri = builder.pathSegment("todos", created.getTodoId()).build().toUri();
        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping(path = "/{todoId}")
    public ResponseEntity<Todo> putTodo(@PathVariable("todoId") String todoId, @RequestBody Todo todo, @AuthenticationPrincipal Jwt jwt) {
        final Optional<Todo> updated = this.todoRepository.findById(todoId)
                .map(t -> {
                    if (todo.getTodoTitle() != null) {
                        t.setTodoTitle(todo.getTodoTitle());
                    }
                    if (!Objects.equals(todo.isFinished(), t.isFinished())) {
                        t.setFinished(todo.isFinished());
                    }
                    t.setUpdatedAt(Instant.now());
                    t.setUpdatedBy(jwt.getSubject());
                    return this.todoRepository.updateById(t);
                });
        return ResponseEntity.of(updated);
    }

    @DeleteMapping(path = "/{todoId}")
    public ResponseEntity<Void> deleteTodo(@PathVariable("todoId") String todoId) {
        this.todoRepository.deleteById(todoId);
        return ResponseEntity.noContent().build();
    }
}
