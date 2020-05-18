package lol.maki.dev.account;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "account")
public class AccountProperties {
    private final List<AccountBuilder> list;

    @ConstructorBinding
    public AccountProperties(List<AccountBuilder> list) {
        this.list = list;
    }

    public Map<String, Account> getAccounts() {
        return this.list.stream()
                .map(AccountBuilder::build)
                .collect(Collectors.toMap(Account::getEmail, Function.identity()));
    }

    static class AccountBuilder {
        private String givenName;
        private String familyName;
        private String email;
        private String password;

        public String getGivenName() {
            return givenName;
        }

        public void setGivenName(String givenName) {
            this.givenName = givenName;
        }

        public String getFamilyName() {
            return familyName;
        }

        public void setFamilyName(String familyName) {
            this.familyName = familyName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Account build() {
            return new Account(this.givenName, this.familyName, this.email, this.password);
        }
    }
}
