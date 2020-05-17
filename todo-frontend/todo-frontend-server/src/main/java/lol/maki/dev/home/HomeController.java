package lol.maki.dev.home;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping(path = "/")
    public ResponseEntity<?> index() {
        return ResponseEntity.status(HttpStatus.SEE_OTHER).header(HttpHeaders.LOCATION, "/index.html").build();
    }

    @GetMapping(path = "/me")
    public Object me(@AuthenticationPrincipal OAuth2AuthenticationToken token) {
        return token.getPrincipal().getAttributes();
    }
}
