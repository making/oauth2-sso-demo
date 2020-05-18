package lol.maki.dev.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@Order(-1)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .formLogin(formLogin -> formLogin
                        .loginPage("/login").usernameParameter("email").passwordParameter("password").permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout") /* supports GET /logout */)
                        .permitAll())
                .requestMatchers(requestMatchers -> requestMatchers
                        .mvcMatchers("/", "/login", "/logout", "/oauth/authorize", "token_keys", "/.well-known/*", "/oauth/token/.well-known/*")
                        .requestMatchers(EndpointRequest.toAnyEndpoint()))
                .authorizeRequests(authorize -> authorize
                        .mvcMatchers("/oauth/authorize", "/token_keys", "/.well-known/*", "/oauth/token/.well-known/*").permitAll()
                        .requestMatchers(EndpointRequest.to("info", "health", "prometheus")).permitAll()
                        .anyRequest().authenticated());
    }
}
