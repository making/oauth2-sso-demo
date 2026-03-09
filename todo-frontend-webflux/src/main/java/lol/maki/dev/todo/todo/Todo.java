package lol.maki.dev.todo.todo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Todo(String todoId, String todoTitle, boolean finished, Instant createdAt, String createdBy,
		Instant updatedAt, String updatedBy) {
}