package lol.maki.dev.authorization;

import java.nio.file.Path;
import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Cookie;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RememberMeE2eTests {

	@LocalServerPort
	int port;

	@TempDir
	static Path tempDir;

	static Playwright playwright;

	static Browser browser;

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + tempDir.resolve("test.db").toAbsolutePath());
	}

	@BeforeAll
	static void setupPlaywright() {
		playwright = Playwright.create();
		browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
	}

	@AfterAll
	static void teardownPlaywright() {
		if (browser != null) {
			browser.close();
		}
		if (playwright != null) {
			playwright.close();
		}
	}

	void loginWithRememberMe(Page page, String username, String password) {
		page.navigate("http://localhost:" + port + "/login");
		page.fill("#username", username);
		page.fill("#password", password);
		page.check("#remember-me");
		page.click("button[type=submit]");
		page.waitForURL("**/");
	}

	void loginWithoutRememberMe(Page page, String username, String password) {
		page.navigate("http://localhost:" + port + "/login");
		page.fill("#username", username);
		page.fill("#password", password);
		page.click("button[type=submit]");
		page.waitForURL("**/");
	}

	@Test
	void shouldSetRememberMeCookieWhenChecked() {
		try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
			loginWithRememberMe(page, "john@example.com", "password");
			assertThat(page.locator(".username")).hasText("john@example.com");

			List<Cookie> cookies = context.cookies();
			boolean hasRememberMeCookie = cookies.stream().anyMatch(cookie -> "remember-me".equals(cookie.name));
			org.assertj.core.api.Assertions.assertThat(hasRememberMeCookie).isTrue();
		}
	}

	@Test
	void shouldRemainAuthenticatedAfterSessionDeletion() {
		try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
			loginWithRememberMe(page, "john@example.com", "password");
			assertThat(page.locator(".username")).hasText("john@example.com");

			// Delete SESSION cookie but keep remember-me cookie
			List<Cookie> cookies = context.cookies();
			context.clearCookies(new BrowserContext.ClearCookiesOptions().setName("SESSION"));

			// Reload the page - should still be authenticated via remember-me
			page.navigate("http://localhost:" + port + "/");
			assertThat(page.locator(".username")).hasText("john@example.com");
		}
	}

	@Test
	void shouldRedirectToLoginAfterSessionDeletionWithoutRememberMe() {
		try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
			loginWithoutRememberMe(page, "john@example.com", "password");
			assertThat(page.locator(".username")).hasText("john@example.com");

			// Delete SESSION cookie
			context.clearCookies(new BrowserContext.ClearCookiesOptions().setName("SESSION"));

			// Reload the page - should redirect to login
			page.navigate("http://localhost:" + port + "/");
			page.waitForURL("**/login");
			assertThat(page.locator(".title")).hasText("Sign in");
		}
	}

}
