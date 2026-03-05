package lol.maki.dev.todo;

import java.time.Instant;
import java.time.InstantSource;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.util.IdGenerator;
import org.springframework.util.SimpleIdGenerator;

@TestConfiguration
public class MockConfig {

	@Bean
	@Primary
	public IdGenerator simpleIdGenerator() {
		return new SimpleIdGenerator();
	}

	@Bean
	@Primary
	public InstantSource incrementalInstantSource() {
		Instant base = Instant.parse("2024-11-01T00:00:00Z");
		AtomicInteger counter = new AtomicInteger(0);
		return () -> base.plusSeconds(counter.getAndIncrement());
	}

}
