package lol.maki.dev.todo;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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
	public Clock incrementalClock() {
		Instant base = Instant.parse("2024-11-01T00:00:00Z");
		AtomicInteger counter = new AtomicInteger(0);
		return new Clock() {

			@Override
			public ZoneId getZone() {
				return ZoneId.of("UTC");
			}

			@Override
			public Clock withZone(ZoneId zone) {
				return this; // ignore
			}

			@Override
			public Instant instant() {
				return base.plusSeconds(counter.getAndIncrement());
			}
		};
	}

}
