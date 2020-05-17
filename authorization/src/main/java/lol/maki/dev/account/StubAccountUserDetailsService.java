package lol.maki.dev.account;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class StubAccountUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // FIXME
        final Account account = new Account("Taro", "Yamada", username, "{noop}password");
        return new AccountUserDetails(account);
    }
}
