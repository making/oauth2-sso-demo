package lol.maki.dev.todo;

import am.ik.yavi.arguments.Arguments;
import am.ik.yavi.arguments.Arguments2;
import am.ik.yavi.arguments.Arguments2Validator;
import am.ik.yavi.arguments.Arguments7;
import am.ik.yavi.arguments.Arguments7Validator;
import am.ik.yavi.arguments.BooleanValidator;
import am.ik.yavi.arguments.ObjectValidator;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.BooleanValidatorBuilder;
import am.ik.yavi.builder.ObjectValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.Constraint;
import am.ik.yavi.validator.Yavi;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;
import org.jilt.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Todo(UUID todoId, String todoTitle, boolean finished, Instant createdAt, String createdBy,
		Instant updatedAt, String updatedBy) {

	public static ObjectValidator<UUID, UUID> todoIdValidator = ObjectValidatorBuilder
		.<UUID>of("todoId", Constraint::notNull)
		.build();

	public static StringValidator<String> todoTitleValidator = StringValidatorBuilder
		.of("todoTitle", c -> c.notBlank().lessThanOrEqual(255))
		.build();

	public static BooleanValidator<Boolean> finishedValidator = BooleanValidatorBuilder
		.of("finished", Constraint::notNull)
		.build();

	private static final Function<String, Arguments2Validator<Instant, String, Arguments2<Instant, String>>> auditInfoValidator = name -> Yavi
		.arguments()
		._instant(name + "At", Constraint::notNull)
		._string(name + "By", c -> c.notBlank().lessThanOrEqual(255))
		.apply(Arguments::of);

	public static final Arguments2Validator<Instant, String, Arguments2<Instant, String>> createdValidator = auditInfoValidator
		.apply("created");

	public static final Arguments2Validator<Instant, String, Arguments2<Instant, String>> updatedValidator = auditInfoValidator
		.apply("updated");

	public static final Arguments7Validator<UUID, String, Boolean, Instant, String, Instant, String, Todo> validator = Arguments7Validator
		.unwrap(todoIdValidator.split(todoTitleValidator)
			.split(finishedValidator)
			.apply(Arguments::of)
			.<Arguments7<UUID, String, Boolean, Instant, String, Instant, String>>compose(Arguments7::first3)
			.combine(createdValidator.compose(args -> Arguments.of(args.arg4(), args.arg5())))
			.combine(updatedValidator.compose(Arguments7::last2))
			.apply((a1, a2, a3) -> new Todo(a1.arg1(), a1.arg2(), a1.arg3(), a2.arg1(), a2.arg2(), a3.arg1(),
					a3.arg2())));

	@Builder(className = "TodoBuilder", factoryMethod = "todo", toBuilder = "from", packageName = "lol.maki.dev.todo")
	public static Todo create(UUID todoId, String todoTitle, boolean finished, Instant createdAt, String createdBy,
			Instant updatedAt, String updatedBy) {
		return validator.validated(todoId, todoTitle, finished, createdAt, createdBy, updatedAt, updatedBy);
	}
}
