package lol.maki.dev.todo;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Todo {
	private static final long serialVersionUID = 1L;

	private String todoId;

	private String todoTitle;

	private boolean finished = false;

	private Instant createdAt;

	private String createdBy;

	private Instant updatedAt;

	private String updatedBy;

	public Todo initializedBy(String createdBy) {
		this.setTodoId(UUID.randomUUID().toString());
		this.setCreatedAt(Instant.now());
		this.setCreatedBy(createdBy);
		this.setUpdatedAt(this.getCreatedAt());
		this.setUpdatedBy(this.getCreatedBy());
		return this;
	}

	public Todo updatedBy(String todoTitle, boolean finished, String updatedBy) {
		if (todoTitle != null) {
			this.setTodoTitle(todoTitle);
		}
		if (!Objects.equals(finished, this.isFinished())) {
			this.setFinished(finished);
		}
		this.setUpdatedAt(Instant.now());
		this.setUpdatedBy(updatedBy);
		return this;
	}

	public String getTodoId() {
		return todoId;
	}

	public void setTodoId(String todoId) {
		this.todoId = todoId;
	}

	public String getTodoTitle() {
		return todoTitle;
	}

	public void setTodoTitle(String todoTitle) {
		this.todoTitle = todoTitle;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
}
