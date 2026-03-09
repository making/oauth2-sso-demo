package lol.maki.dev.authorization;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
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
class ChangePasswordE2eTests {

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

	void loginWithPassword(Page page, String username, String password) {
		page.navigate("http://localhost:" + port + "/login");
		page.fill("#username", username);
		page.fill("#password", password);
		page.click("button[type=submit]");
		page.waitForURL("**/");
	}

	@Test
	void shouldChangePasswordAndLoginWithNewPassword() {
		try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
			// Step 1: Login with current password
			loginWithPassword(page, "john@example.com", "password");
			assertThat(page.locator(".username")).hasText("john@example.com");

			// Step 2: Navigate to change password page
			page.click("a[href='/change-password']");
			page.waitForURL("**/change-password");
			assertThat(page.locator(".title")).hasText("Change Password");

			// Step 3: Change password
			page.fill("#currentPassword", "password");
			page.fill("#newPassword", "newpassword123");
			page.fill("#confirmNewPassword", "newpassword123");
			page.click("button[type=submit]");

			// Step 4: Verify redirect to home with success message
			page.waitForURL("**/?passwordChanged");
			assertThat(page.locator(".success-banner")).isVisible();

			// Step 5: Logout
			page.navigate("http://localhost:" + port + "/logout");
			page.click("button[type=submit]");
			page.waitForURL("**/login");

			// Step 6: Login with new password
			loginWithPassword(page, "john@example.com", "newpassword123");
			assertThat(page.locator(".username")).hasText("john@example.com");
		}
	}

}
