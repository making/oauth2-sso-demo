package lol.maki.dev.todo.todo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

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

		private UUID todoId;

		private String todoTitle;

		private boolean finished;

		private Instant createdAt;

		private String createdBy;

		private Instant updatedAt;

		private String updatedBy;

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
			return new Todo(this.todoId, this.todoTitle, this.finished, this.createdAt, this.createdBy, this.updatedAt,
					this.updatedBy);
		}

	}

}
