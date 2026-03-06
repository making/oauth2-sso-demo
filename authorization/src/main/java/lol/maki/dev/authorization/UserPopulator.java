package lol.maki.dev.authorization;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.security.autoconfigure.SecurityProperties;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class UserPopulator implements InitializingBean {

	private final SecurityProperties properties;

	private final JdbcUserDetailsManager userDetailsManager;

	private final PasswordEncoder passwordEncoder;

	public UserPopulator(SecurityProperties properties, JdbcUserDetailsManager userDetailsManager,
			PasswordEncoder passwordEncoder) {
		this.properties = properties;
		this.userDetailsManager = userDetailsManager;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void afterPropertiesSet() {
		SecurityProperties.User user = this.properties.getUser();
		List<String> roles = user.getRoles();
		UserDetails userDetails = User.withUsername(user.getName())
			.passwordEncoder(rawPassword -> (rawPassword != null && rawPassword.startsWith("{")) ? rawPassword
					: this.passwordEncoder.encode(rawPassword))
			.password(user.getPassword())
			.roles(StringUtils.toStringArray(roles))
			.build();
		if (this.userDetailsManager.userExists(userDetails.getUsername())) {
			this.userDetailsManager.updateUser(userDetails);
		}
		else {
			this.userDetailsManager.createUser(userDetails);
		}
	}

}
