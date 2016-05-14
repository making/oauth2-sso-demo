package com.example;

import org.apache.catalina.filters.RequestDumperFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
@SpringBootApplication
@EnableZuulProxy
@EnableOAuth2Sso
public class UiApplication {

    @Controller
    static class HomeController {

        @RequestMapping("/")
        String home(Model model) {
            return "index";
        }
    }

    public static class Message {
        public String text;
        public String username;
        public LocalDateTime createdAt;
    }


    public static void main(String[] args) {
        SpringApplication.run(UiApplication.class, args);
    }

    @Profile("!cloud")
    @Bean
    RequestDumperFilter requestDumperFilter() {
        return new RequestDumperFilter();
    }
}
