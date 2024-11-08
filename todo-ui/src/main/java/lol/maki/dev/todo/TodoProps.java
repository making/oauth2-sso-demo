package lol.maki.dev.todo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "todo")
public record TodoProps(@DefaultValue("http://localhost:8081") String apiUrl) {
}
