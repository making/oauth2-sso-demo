package lol.maki.dev.authorization;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class PasskeyE2eTests {

	static final int port = findAvailablePort();

	@TempDir
	static Path tempDir;

	static Playwright playwright;

	static Browser browser;

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + tempDir.resolve("test.db").toAbsolutePath());
		registry.add("server.port", () -> port);
		registry.add("webauthn.rp-id", () -> "localhost");
		registry.add("webauthn.allowed-origins", () -> "http://localhost:" + port);
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

	CDPSession setupVirtualAuthenticator(Page page) {
		CDPSession cdpSession = page.context().newCDPSession(page);
		cdpSession.send("WebAuthn.enable");

		JsonObject options = new JsonObject();
		options.addProperty("protocol", "ctap2");
		options.addProperty("transport", "internal");
		options.addProperty("hasResidentKey", true);
		options.addProperty("hasUserVerification", true);
		options.addProperty("isUserVerified", true);

		JsonObject params = new JsonObject();
		params.add("options", options);
		cdpSession.send("WebAuthn.addVirtualAuthenticator", params);

		return cdpSession;
	}

	void loginWithPassword(Page page) {
		page.navigate("http://localhost:" + port + "/login");
		page.fill("#username", "john@example.com");
		page.fill("#password", "password");
		page.click("button[type=submit]");
		page.waitForURL("**/");
	}

	@Test
	void shouldRegisterPasskeyAndLoginWithIt() {
		try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
			CDPSession cdpSession = setupVirtualAuthenticator(page);
			try {
				// Step 1: Login with password
				loginWithPassword(page);

				// Step 2: Navigate to passkeys page and register a passkey
				page.navigate("http://localhost:" + port + "/passkeys");
				assertThat(page.locator(".card-title")).hasText("Your passkeys");

				// Click add passkey button
				page.click(".btn-add");
				page.waitForSelector("#add-modal[style*='flex']");

				// Enter label and add
				page.fill("#passkey-name", "Test Passkey");
				page.click("#btn-add-passkey");

				// Wait for redirect after registration
				page.waitForURL("**/passkeys?success", new Page.WaitForURLOptions().setTimeout(15000));

				// Verify passkey appears in list
				assertThat(page.locator(".passkey-name")).hasText("Test Passkey");

				// Step 3: Logout
				page.navigate("http://localhost:" + port + "/logout");
				page.click("button[type=submit]");
				page.waitForURL("**/login");

				// Step 4: Login with passkey
				page.click("#passkey-signin");

				// Wait for navigation to home after passkey login
				page.waitForURL("**/", new Page.WaitForURLOptions().setTimeout(10000));

				// Step 5: Verify home page is displayed
				assertThat(page.locator(".username")).hasText("john@example.com");
			}
			finally {
				cdpSession.send("WebAuthn.disable");
			}
		}
	}

	@Test
	void shouldDeletePasskey() {
		try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
			CDPSession cdpSession = setupVirtualAuthenticator(page);

			try {
				// Login and register a passkey
				loginWithPassword(page);
				page.navigate("http://localhost:" + port + "/passkeys");

				page.click(".btn-add");
				page.waitForSelector("#add-modal[style*='flex']");
				page.fill("#passkey-name", "To Delete");
				page.click("#btn-add-passkey");
				page.waitForURL("**/passkeys?success", new Page.WaitForURLOptions().setTimeout(15000));

				// Count passkeys before delete
				int countBefore = page.locator(".passkey-item").count();

				// Accept the confirmation dialog
				page.onDialog(dialog -> dialog.accept());

				// Click the last delete button (the one we just registered)
				page.locator(".delete-icon").last().click();

				// Verify one passkey is removed
				assertThat(page.locator(".passkey-item")).hasCount(countBefore - 1);
			}
			finally {
				cdpSession.send("WebAuthn.disable");
			}
		}
	}

	static int findAvailablePort() {
		try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
			return socket.getLocalPort();
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
