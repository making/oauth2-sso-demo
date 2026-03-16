package lol.maki.dev.authorization;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "remember-me")
public record RememberMeProps(@DefaultValue("14d") Duration tokenValidity) {
}
