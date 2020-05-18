package lol.maki.dev.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "oauth")
public class OauthProperties {
    private final List<BaseClientDetails> clients;

    @ConstructorBinding
    public OauthProperties(List<BaseClientDetails> clients) {
        this.clients = clients;
    }

    public Map<String, ClientDetails> getClients() {
        return clients.stream().collect(Collectors.toMap(ClientDetails::getClientId, x -> x));
    }
}
