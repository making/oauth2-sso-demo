package lol.maki.dev.todo;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@Import({ TestcontainersConfiguration.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = { "server.port=52241" })
class TodoFrontendApplicationTests {

	static Playwright playwright;

	static Browser browser;

	BrowserContext context;

	Page page;

	@MockitoBean
	TodoClient todoClient;

	@BeforeAll
	static void before() {
		playwright = Playwright.create();
		browser = playwright.chromium().launch();
	}

	@BeforeEach
	void beforeEach() {
		context = browser.newContext();
		context.setDefaultTimeout(3000);
		page = context.newPage();
	}

	@AfterEach
	void afterEach() {
		context.close();
	}

	@AfterAll
	static void after() {
		playwright.close();
	}

	Todo todo1 = new Todo(UUID.randomUUID().toString(), "Todo 1", false, Instant.now(), "admin", Instant.now(),
			"admin");

	Todo todo2 = new Todo(UUID.randomUUID().toString(), "Todo 2", true, Instant.now(), "admin", Instant.now(), "admin");

	Todo todo3 = new Todo(UUID.randomUUID().toString(), "Todo 3", false, Instant.now(), "admin", Instant.now(),
			"admin");

	static String localDateString(Instant instant) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy, h:mm a");
		return localDateTime.format(formatter);
	}

	void login(String username, String password) {
		page.navigate("http://localhost:52241");
		assertThat(page.title()).isEqualTo("Login");
		page.locator("input[name=username]").fill(username);
		page.locator("input[name=password]").fill(password);
		page.locator("button[type=submit]").press("Enter");
	}

	@Test
	void shouldShowTodoListAfterSuccessfulLogin() {
		given(this.todoClient.listTodos()).willReturn(List.of(todo1, todo2, todo3));
		this.login("test@example.com", "test");
		assertThat(page.locator("h1 + p").textContent()).isEqualTo("Welcome, test@example.com!");
		assertThat(page.title()).isEqualTo("Todo List");
		Locator rows = page.locator("tbody > tr");
		assertThat(rows.count()).isEqualTo(3);
		assertThat(rows.nth(0).textContent())
			.isEqualTo("%s%s%s%s%s".formatted(todo1.todoTitle(), localDateString(todo1.createdAt()), todo1.createdBy(),
					localDateString(todo1.updatedAt()), todo1.updatedBy()));
		assertThat(rows.nth(0).locator("button[title=\"Mark as complete\"]").count()).isEqualTo(1);
		assertThat(rows.nth(1).textContent())
			.isEqualTo("%s%s%s%s%s".formatted(todo2.todoTitle(), localDateString(todo2.createdAt()), todo2.createdBy(),
					localDateString(todo2.updatedAt()), todo2.updatedBy()));
		assertThat(rows.nth(1).locator("button[title=\"Mark as incomplete\"]").count()).isEqualTo(1);
		assertThat(rows.nth(2).textContent())
			.isEqualTo("%s%s%s%s%s".formatted(todo3.todoTitle(), localDateString(todo3.createdAt()), todo3.createdBy(),
					localDateString(todo3.updatedAt()), todo3.updatedBy()));
		assertThat(rows.nth(2).locator("button[title=\"Mark as complete\"]").count()).isEqualTo(1);
	}

	@Disabled("TODO")
	@Test
	void shouldShowNewTodoAfterCreating() {
		given(this.todoClient.listTodos()).willReturn(List.of(todo1));
		given(this.todoClient.postTodo(any()))
			.will(invocation -> new Todo(todo2.todoId(), invocation.getArgument(0, Todo.class).todoTitle(), false,
					todo2.createdAt(), todo2.createdBy(), todo2.updatedAt(), todo2.updatedBy()));
		this.login("test@example.com", "test");
		assertThat(page.locator("h1 + p").textContent()).isEqualTo("Welcome, test@example.com!");
		assertThat(page.locator("tbody > tr").count()).isEqualTo(1);
		page.getByPlaceholder("What needs to be done?").fill("Test Todo!");
		page.locator("button[type=submit]").press("Enter");
		page.waitForCondition(() -> page.locator("tbody > tr").count() == 2);
		Locator rows = page.locator("tbody > tr");
		assertThat(rows.nth(0).textContent())
			.isEqualTo("%s%s%s%s%s".formatted(todo1.todoTitle(), localDateString(todo1.createdAt()), todo1.createdBy(),
					localDateString(todo1.updatedAt()), todo1.updatedBy()));
		assertThat(rows.nth(0).locator("button[title=\"Mark as complete\"]").count()).isEqualTo(1);
		assertThat(rows.nth(1).textContent())
			.isEqualTo("%s%s%s%s%s".formatted("Test Todo!", localDateString(todo2.createdAt()), todo2.createdBy(),
					localDateString(todo2.updatedAt()), todo2.updatedBy()));
		assertThat(rows.nth(1).locator("button[title=\"Mark as complete\"]").count()).isEqualTo(1);
	}

	@Test
	void shouldCompleteTodoAfterClickingCheckButton() {
		Todo patched = new Todo(todo1.todoId(), todo1.todoTitle(), true, todo1.createdAt(), todo1.createdBy(),
				todo1.updatedAt(), todo1.updatedBy());
		given(this.todoClient.listTodos()).willReturn(List.of(todo1)).willReturn(List.of(patched));
		given(this.todoClient.patchTodo(any(), any())).willReturn(patched);
		this.login("test@example.com", "test");
		assertThat(page.locator("h1 + p").textContent()).isEqualTo("Welcome, test@example.com!");
		page.locator("tbody > tr").first().locator("button").first().click();
		page.waitForCondition(
				() -> page.locator("tbody > tr").nth(0).locator("button[title=\"Mark as incomplete\"]").count() == 1);
		verify(this.todoClient).patchTodo(any(), any());
	}

	@Test
	void shouldIncompleteTodoAfterClickingCheckButton() {
		Todo patched = new Todo(todo2.todoId(), todo2.todoTitle(), false, todo2.createdAt(), todo2.createdBy(),
				todo2.updatedAt(), todo2.updatedBy());
		given(this.todoClient.listTodos()).willReturn(List.of(todo2)).willReturn(List.of(patched));
		given(this.todoClient.patchTodo(any(), any())).willReturn(patched);
		this.login("test@example.com", "test");
		assertThat(page.locator("h1 + p").textContent()).isEqualTo("Welcome, test@example.com!");
		assertThat(page.locator("tbody > tr").nth(0).locator("button[title=\"Mark as incomplete\"]").count())
			.isEqualTo(1);
		page.locator("tbody > tr").first().locator("button").first().click();
		page.waitForCondition(
				() -> page.locator("tbody > tr").nth(0).locator("button[title=\"Mark as complete\"]").count() == 1);
		verify(this.todoClient).patchTodo(any(), any());
	}

	@Test
	void shouldHideCompleteTodosAfterCheckingHideButton() {
		given(this.todoClient.listTodos()).willReturn(List.of(todo1, todo2, todo3));
		this.login("test@example.com", "test");
		assertThat(page.locator("h1 + p").textContent()).isEqualTo("Welcome, test@example.com!");
		assertThat(page.locator("tbody > tr").count()).isEqualTo(3);
		page.getByText("Hide completed").check();
		page.waitForCondition(() -> page.locator("tbody > tr").count() == 2);
	}

	@Test
	void shouldDeleteTodoAfterClickingTrashButton() {
		given(this.todoClient.listTodos()).willReturn(List.of(todo1, todo2, todo3)).willReturn(List.of(todo2, todo2));
		given(this.todoClient.patchTodo(any(), any())).willReturn(new Todo(todo1.todoId(), todo1.todoTitle(), true,
				todo1.createdAt(), todo1.createdBy(), todo1.updatedAt(), todo1.updatedBy()));
		this.login("test@example.com", "test");
		assertThat(page.locator("h1 + p").textContent()).isEqualTo("Welcome, test@example.com!");
		assertThat(page.locator("tbody > tr").count()).isEqualTo(3);
		page.locator("tbody > tr").first().locator("button").nth(1).click();
		page.waitForCondition(() -> page.locator("tbody > tr").count() == 2);
		verify(this.todoClient).deleteTodo(todo1.todoId());
	}

	@Test
	void shouldShowErrorMessageAfterFailedLogin() {
		this.login("test@example.com", "qwerty");
		assertThat(page.locator("div.error-message").textContent()).isNotEmpty();
	}

}
