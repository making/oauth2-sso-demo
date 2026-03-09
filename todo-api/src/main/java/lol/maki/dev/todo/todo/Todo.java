package lol.maki.dev.todo.todo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Todo(UUID todoId, String todoTitle, boolean finished, Instant createdAt, String createdBy,
		Instant updatedAt, String updatedBy) {

	public static TodoBuilder builder() {
		return new TodoBuilder();
	}

	public TodoBuilder toBuilder() {
		return new TodoBuilder().todoId(this.todoId)
			.todoTitle(this.todoTitle)
			.finished(this.finished)
			.createdAt(this.createdAt)
			.createdBy(this.createdBy)
			.updatedAt(this.updatedAt)
			.updatedBy(this.updatedBy);
	}

	public static class TodoBuilder {

		private TodoBuilder() {
		}

		@Nullable private UUID todoId;

		@Nullable private String todoTitle;

		private boolean finished;

		@Nullable private Instant createdAt;

		@Nullable private String createdBy;

		@Nullable private Instant updatedAt;

		@Nullable private String updatedBy;

		public TodoBuilder todoId(UUID todoId) {
			this.todoId = todoId;
			return this;
		}

		public TodoBuilder todoTitle(String todoTitle) {
			this.todoTitle = todoTitle;
			return this;
		}

		public TodoBuilder finished(boolean finished) {
			this.finished = finished;
			return this;
		}

		public TodoBuilder createdAt(Instant createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public TodoBuilder createdBy(String createdBy) {
			this.createdBy = createdBy;
			return this;
		}

		public TodoBuilder updatedAt(Instant updatedAt) {
			this.updatedAt = updatedAt;
			return this;
		}

		public TodoBuilder updatedBy(String updatedBy) {
			this.updatedBy = updatedBy;
			return this;
		}

		public Todo build() {
			return new Todo(Objects.requireNonNull(this.todoId, "todoId must be set before calling build()"),
					Objects.requireNonNull(this.todoTitle, "todoTitle must be set before calling build()"),
					this.finished,
					Objects.requireNonNull(this.createdAt, "createdAt must be set before calling build()"),
					Objects.requireNonNull(this.createdBy, "createdBy must be set before calling build()"),
					Objects.requireNonNull(this.updatedAt, "updatedAt must be set before calling build()"),
					Objects.requireNonNull(this.updatedBy, "updatedBy must be set before calling build()"));
		}

	}

}
