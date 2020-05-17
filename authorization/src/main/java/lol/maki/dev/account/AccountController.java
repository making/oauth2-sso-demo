package lol.maki.dev.account;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    @GetMapping(path = "/")
    public String index(Model model, @AuthenticationPrincipal AccountUserDetails userDetails) {
        model.addAttribute("account", userDetails.getAccount());
        return "index";
    }

    @GetMapping(path = "login")
    public String login() {
        return "login";
    }
}
