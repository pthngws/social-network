package com.phithang.mysocialnetwork.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HomeController {

    @GetMapping("/")
    public String login() {
        return "login";
    }
    @GetMapping("/login")
    public String login2() {
        return "login";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/search/{name}")
    public String search() {
        return "listuser";
    }

    @GetMapping("/{id}")
    public String user() {
        return "user";
    }
}
