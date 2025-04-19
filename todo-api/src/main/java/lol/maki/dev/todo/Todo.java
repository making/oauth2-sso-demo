package lol.maki.dev.todo;

import am.ik.yavi.arguments.Arguments7Validator;
import am.ik.yavi.arguments.BooleanValidator;
import am.ik.yavi.arguments.InstantValidator;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.BooleanValidatorBuilder;
import am.ik.yavi.builder.InstantValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.function.Function;
import org.jilt.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Todo(String todoId, String todoTitle, boolean finished, Instant createdAt, String createdBy,
		Instant updatedAt, String updatedBy) {

	private static Function<String, InstantValidator<Instant>> instantValidator = (
			name) -> InstantValidatorBuilder.of(name, c -> c.notNull()).build();

	private static Function<String, StringValidator<String>> usernameValidator = (
			name) -> StringValidatorBuilder.of(name, c -> c.email().notBlank().lessThanOrEqual(255)).build();

	public static StringValidator<String> todoIdValidator = StringValidatorBuilder
		.of("todoId", c -> c.notBlank().lessThanOrEqual(255))
		.build();

	public static StringValidator<String> todoTitleValidator = StringValidatorBuilder
		.of("todoTitle", c -> c.notBlank().lessThanOrEqual(255))
		.build();

	public static BooleanValidator<Boolean> finishedValidator = BooleanValidatorBuilder.of("finished", c -> c.notNull())
		.build();

	public static InstantValidator<Instant> createdAtValidator = instantValidator.apply("createdAt");

	public static InstantValidator<Instant> updatedAtValidator = instantValidator.apply("updatedAt");

	public static StringValidator<String> createdByValidator = usernameValidator.apply("createdBy");

	public static StringValidator<String> updatedByValidator = usernameValidator.apply("updatedBy");

	public static Arguments7Validator<String, String, Boolean, Instant, String, Instant, String, Todo> validator = todoIdValidator
		.split(todoTitleValidator)
		.split(finishedValidator)
		.split(createdAtValidator)
		.split(createdByValidator)
		.split(updatedAtValidator)
		.split(updatedByValidator)
		.apply(Todo::new);

	@Builder(className = "TodoBuilder", factoryMethod = "todo", toBuilder = "from", packageName = "lol.maki.dev.todo")
	public static Todo create(String todoId, String todoTitle, boolean finished, Instant createdAt, String createdBy,
			Instant updatedAt, String updatedBy) {
		return validator.validated(todoId, todoTitle, finished, createdAt, createdBy, updatedAt, updatedBy);
	}
}
