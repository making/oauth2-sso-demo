package lol.maki.dev.todo.todo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;
import org.jilt.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Todo(UUID todoId, String todoTitle, boolean finished, Instant createdAt, String createdBy,
		Instant updatedAt, String updatedBy) {

	@Builder(className = "TodoBuilder", factoryMethod = "todo", toBuilder = "from", packageName = "lol.maki.dev.todo")
	public static Todo create(UUID todoId, String todoTitle, boolean finished, Instant createdAt, String createdBy,
			Instant updatedAt, String updatedBy) {
		return new Todo(todoId, todoTitle, finished, createdAt, createdBy, updatedAt, updatedBy);
	}
}
