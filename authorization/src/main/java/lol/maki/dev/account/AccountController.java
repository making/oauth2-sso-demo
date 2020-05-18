package lol.maki.dev.account;

import lol.maki.dev.config.OauthProperties;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AccountController {
    private final List<ClientDetails> clientDetails;

    public AccountController(OauthProperties oauthProperties) {
        this.clientDetails = oauthProperties.getClients().values()
                .stream()
                .filter(c -> c.getAdditionalInformation().containsKey("name"))
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/")
    public String index(Model model, @AuthenticationPrincipal AccountUserDetails userDetails) {
        model.addAttribute("account", userDetails.getAccount());
        model.addAttribute("clientDetails", clientDetails);
        return "index";
    }

    @GetMapping(path = "login")
    public String login() {
        return "login";
    }
}
