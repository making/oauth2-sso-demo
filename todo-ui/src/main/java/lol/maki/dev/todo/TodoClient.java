package lol.maki.dev.todo;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(url = "/todos")
public interface TodoClient {

	@GetExchange
	List<Todo> listTodos();

	@GetExchange(url = "/{todoId}")
	Todo getTodo(@PathVariable("todoId") String todoId);

	@PostExchange
	Todo postTodo(@RequestBody Todo todo);

	@PatchExchange(url = "/{todoId}")
	Todo patchTodo(@PathVariable("todoId") String todoId, @RequestBody Todo todo);

	@DeleteExchange(url = "/{todoId}")
	void deleteTodo(@PathVariable("todoId") String todoId);

}
