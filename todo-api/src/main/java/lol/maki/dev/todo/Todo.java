package lol.maki.dev.todo;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jilt.Builder;

@Builder(toBuilder = "from")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Todo(String todoId, String todoTitle, boolean finished, Instant createdAt, String createdBy,
		Instant updatedAt, String updatedBy) {

}
