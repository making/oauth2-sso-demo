package lol.maki.dev.todo;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TodoRepository {
    private final Map<String, Todo> map = Collections.synchronizedMap(new LinkedHashMap<>());

    public Optional<Todo> findById(String todoId) {
        return Optional.ofNullable(this.map.get(todoId));
    }

    public List<Todo> findAll() {
        return new ArrayList<>(this.map.values());
    }

    public Todo create(Todo todo) {
        this.map.put(todo.getTodoId(), todo);
        return todo;
    }

    public Todo updateById(Todo todo) {
        return this.map.put(todo.getTodoId(), todo);
    }

    public void deleteById(String todoId) {
        this.map.remove(todoId);
    }

    void clear() {
        this.map.clear();
    }
}
