package lol.maki.dev.account;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InMemoryAccountUserDetailsService implements UserDetailsService {
    private final Map<String, Account> accounts;

    public InMemoryAccountUserDetailsService(AccountProperties properties) {
        this.accounts = properties.getAccounts();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Account account = this.accounts.get(username /* email */);
        if (account == null) {
            throw new UsernameNotFoundException(String.format("%s is not found.", username));
        }
        return new AccountUserDetails(account);
    }
}
