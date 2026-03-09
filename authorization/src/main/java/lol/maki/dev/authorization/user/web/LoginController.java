package lol.maki.dev.authorization.user.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

	@GetMapping(path = "/login")
	public String login() {
		return "user/login";
	}

	@GetMapping(path = "/logout")
	public String logout() {
		return "user/logout";
	}

}
