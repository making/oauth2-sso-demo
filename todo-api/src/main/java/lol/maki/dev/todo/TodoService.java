package lol.maki.dev.todo;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.IdGenerator;

@Service
public class TodoService {

	private final TodoRepository todoRepository;

	private final IdGenerator idGenerator;

	private final Clock clock;

	public TodoService(TodoRepository todoRepository, IdGenerator idGenerator, Clock clock) {
		this.todoRepository = todoRepository;
		this.idGenerator = idGenerator;
		this.clock = clock;
	}

	public List<Todo> getTodos() {
		return this.todoRepository.findAll();
	}

	public Todo getTodo(UUID todoId) {
		return this.todoRepository.findById(todoId).orElseThrow(() -> new NotFoundException(todoId));
	}

	public Todo create(String todoTitle, String email) {
		Instant now = this.clock.instant();
		Todo todo = TodoBuilder.todo()
			.todoId(this.idGenerator.generateId())
			.todoTitle(todoTitle)
			.finished(false)
			.createdBy(email)
			.createdAt(now)
			.updatedBy(email)
			.updatedAt(now)
			.build();
		Todo updated = this.todoRepository.save(todo);
		return this.todoRepository.save(updated);
	}

	public Todo update(UUID todoId, String todoTitle, boolean finished, String username) {
		return this.todoRepository.findById(todoId).map(t -> {
			TodoBuilder builder = TodoBuilder.from(t);
			boolean touched = false;
			if (todoTitle != null) {
				builder.todoTitle(todoTitle);
				touched = true;
			}
			if (!Objects.equals(finished, t.finished())) {
				builder.finished(finished);
				touched = true;
			}
			if (touched) {
				builder.updatedBy(username);
				builder.updatedAt(this.clock.instant());
			}
			return builder.build();
		}).map(this.todoRepository::save).orElseThrow(() -> new NotFoundException(todoId));
	}

	public void deleteById(UUID todoId) {
		this.todoRepository.deleteById(todoId);
	}

	public static class NotFoundException extends RuntimeException {

		public NotFoundException(UUID todoId) {
			super("Todo not found: todoId=%s".formatted(todoId));
		}

	}

}
