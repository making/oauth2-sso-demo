package lol.maki.dev.todo;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.web.server.WebFilter;
import org.springframework.web.util.UriComponentsBuilder;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	static final int SERVER_PORT = 52242;

	static final int AS_PORT = 39877;

	static {
		Testcontainers.exposeHostPorts(SERVER_PORT);
	}

	@Bean
	GenericContainer<?> authorizationServer() {
		GenericContainer<?> container = new GenericContainer<>("ghcr.io/making/oauth2-sso-demo/authorization:jvm")
			.withImagePullPolicy(PullPolicy.alwaysPull())
			.withEnv("spring.security.user.name", "test@example.com")
			.withEnv("spring.security.user.password", "test")
			.withEnv("spring.security.oauth2.authorizationserver.client.todo-frontend.registration.client-id",
					"todo-frontend")
			.withEnv("spring.security.oauth2.authorizationserver.client.todo-frontend.registration.client-secret",
					"{noop}secret")
			.withEnv("spring.security.oauth2.authorizationserver.client.todo-frontend.registration.redirect-uris",
					"http://localhost:" + SERVER_PORT + "/login/oauth2/code/todo-frontend")
			.withEnv(
					"spring.security.oauth2.authorizationserver.client.todo-frontend.registration.post-logout-redirect-uris",
					"http://localhost:" + SERVER_PORT)
			.withEnv("back-channel-logout.issuer", "http://127.0.0.1:" + AS_PORT)
			.withEnv("back-channel-logout.uris.todo-frontend",
					"http://host.testcontainers.internal:" + SERVER_PORT + "/logout/connect/back-channel/todo-frontend")
			.withEnv("spring.http.clients.imperative.factory", "simple")
			.withEnv("spring.main.banner-mode", "off")
			.withExposedPorts(9000)
			.waitingFor(Wait.forHttp("/actuator/health").forPort(9000).withStartupTimeout(Duration.ofSeconds(10)))
			.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("authorization-server")));
		container.setPortBindings(List.of(AS_PORT + ":9000"));
		return container;
	}

	/**
	 * Rewrite the Host of back-channel logout requests from host.testcontainers.internal
	 * to localhost so that OidcBackChannelServerLogoutHandler's internal
	 * session-invalidation requests target localhost (which is always resolvable) instead
	 * of the Testcontainers hostname.
	 */
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	WebFilter backChannelLogoutHostRewriteFilter() {
		return (exchange, chain) -> {
			if ("host.testcontainers.internal".equals(exchange.getRequest().getURI().getHost())) {
				URI rewritten = UriComponentsBuilder.fromUri(exchange.getRequest().getURI())
					.host("localhost")
					.build()
					.toUri();
				ServerHttpRequest mutated = exchange.getRequest().mutate().uri(rewritten).build();
				return chain.filter(exchange.mutate().request(mutated).build());
			}
			return chain.filter(exchange);
		};
	}

	@Bean
	DynamicPropertyRegistrar dynamicPropertyRegistrar(GenericContainer<?> authorizationServer) {
		return registry -> registry.add("spring.security.oauth2.client.provider.todo-frontend.issuer-uri",
				() -> "http://127.0.0.1:" + authorizationServer.getMappedPort(9000));
	}

}
