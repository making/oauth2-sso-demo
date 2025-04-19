package lol.maki.dev.todo;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@HttpExchange(url = "/todos")
public interface TodoClient {

	@GetExchange
	Flux<Todo> listTodos();

	@GetExchange(url = "/{todoId}")
	Mono<Todo> getTodo(@PathVariable("todoId") String todoId);

	@PostExchange
	Mono<Todo> postTodo(@RequestBody Todo todo);

	@PatchExchange(url = "/{todoId}")
	Mono<Todo> patchTodo(@PathVariable("todoId") String todoId, @RequestBody Todo todo);

	@DeleteExchange(url = "/{todoId}")
	Mono<Void> deleteTodo(@PathVariable("todoId") String todoId);

}
