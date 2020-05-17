package lol.maki.dev.account;

public class Account {
    private final String givenName;
    private final String familyName;
    private final String email;
    private final String password;

    public Account(String givenName, String familyName, String email, String password) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.email = email;
        this.password = password;
    }

    public String getFullName() {
        return String.format("%s %s", this.getFamilyName(), this.getGivenName());
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
