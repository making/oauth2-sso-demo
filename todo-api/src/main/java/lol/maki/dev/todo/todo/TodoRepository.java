package lol.maki.dev.todo.todo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class TodoRepository {

	private final JdbcClient jdbcClient;

	public TodoRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public Optional<Todo> findById(UUID todoId) {
		return this.jdbcClient.sql(
				"SELECT todo_id, todo_title, finished, created_at, created_by, updated_at, updated_by FROM todo WHERE todo_id = ?")
			.param(todoId.toString())
			.query(this::mapRow)
			.optional();
	}

	public List<Todo> findAll() {
		return this.jdbcClient.sql(
				"SELECT todo_id, todo_title, finished, created_at, created_by, updated_at, updated_by FROM todo ORDER BY created_at")
			.query(this::mapRow)
			.list();
	}

	public Todo save(Todo todo) {
		this.jdbcClient.sql(
				"INSERT OR REPLACE INTO todo (todo_id, todo_title, finished, created_at, created_by, updated_at, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?)")
			.param(todo.todoId().toString())
			.param(todo.todoTitle())
			.param(todo.finished() ? 1 : 0)
			.param(todo.createdAt().toString())
			.param(todo.createdBy())
			.param(todo.updatedAt().toString())
			.param(todo.updatedBy())
			.update();
		return todo;
	}

	public void deleteById(UUID todoId) {
		this.jdbcClient.sql("DELETE FROM todo WHERE todo_id = ?").param(todoId.toString()).update();
	}

	void clear() {
		this.jdbcClient.sql("DELETE FROM todo").update();
	}

	private Todo mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
		return Todo.builder()
			.todoId(UUID.fromString(rs.getString("todo_id")))
			.todoTitle(rs.getString("todo_title"))
			.finished(rs.getInt("finished") != 0)
			.createdAt(Instant.parse(rs.getString("created_at")))
			.createdBy(rs.getString("created_by"))
			.updatedAt(Instant.parse(rs.getString("updated_at")))
			.updatedBy(rs.getString("updated_by"))
			.build();
	}

}
