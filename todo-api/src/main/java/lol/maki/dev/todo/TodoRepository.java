package lol.maki.dev.todo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TodoRepository {

	private final Map<UUID, Todo> map = Collections.synchronizedMap(new LinkedHashMap<>());

	public Optional<Todo> findById(UUID todoId) {
		return Optional.ofNullable(this.map.get(todoId));
	}

	public List<Todo> findAll() {
		return new ArrayList<>(this.map.values());
	}

	public Todo save(Todo todo) {
		this.map.put(todo.todoId(), todo);
		return todo;
	}

	public void deleteById(UUID todoId) {
		this.map.remove(todoId);
	}

	void clear() {
		this.map.clear();
	}

}
