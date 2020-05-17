package lol.maki.dev.home;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@RestController
@Profile("default")
public class DevProxyController {
    private final WebClient webClient;

    public DevProxyController(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @GetMapping(path = {"index.html", "static/**", "*.json", "*.ico"})
    public Mono<ResponseEntity<ByteBuffer>> proxy(ServerHttpRequest request) {
        return this.webClient.get().uri("http://localhost:3000" + request.getPath())
                .exchange()
                .flatMap(res -> res.toEntity(ByteBuffer.class));
    }
}