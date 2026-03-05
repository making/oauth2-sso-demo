package lol.maki.dev.authorization;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

	@GetMapping(path = "/login")
	public String login() {
		return "login";
	}

	@GetMapping(path = "/")
	public String index(Principal principal, Model model) {
		model.addAttribute("username", principal.getName());
		return "index";
	}

	@GetMapping(path = "/logout")
	public String logout() {
		return "logout";
	}

}
