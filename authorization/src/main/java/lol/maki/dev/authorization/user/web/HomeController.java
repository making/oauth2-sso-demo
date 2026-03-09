package lol.maki.dev.authorization.user.web;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {

	private final JdbcClient jdbcClient;

	public HomeController(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@GetMapping(path = "/")
	public String index(Principal principal, Model model) {
		List<Client> clients = this.jdbcClient.sql("""
				SELECT
				    client_id,
				    client_name,
				    post_logout_redirect_uris
				FROM
				    oauth2_registered_client
				WHERE
				    authorization_grant_types LIKE '%authorization_code%'
				AND post_logout_redirect_uris IS NOT NULL""").query(Client.class).list();
		model.addAttribute("username", principal.getName());
		model.addAttribute("clients", clients);
		return "user/index";
	}

	public record Client(String clientId, String clientName, String postLogoutRedirectUris) {

		String url() {
			return postLogoutRedirectUris.split(",")[0];
		}
	}

}
