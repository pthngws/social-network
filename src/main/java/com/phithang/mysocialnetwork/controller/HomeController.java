package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<ApiResponse<String>> root() {
        return ResponseEntity.ok(new ApiResponse<>(200, "MySocialNetwork API is running", "Welcome to MySocialNetwork API"));
    }

    @GetMapping("/login")
    public ResponseEntity<ApiResponse<String>> login() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Login endpoint", "Please use /auth/login for authentication"));
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<String>> home() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Home endpoint", "Welcome to home"));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<String>> profile() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Profile endpoint", "Profile information"));
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<ApiResponse<String>> search(@PathVariable String name) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Search results for: " + name, "Search functionality"));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResponse<String>> user(@PathVariable String id) {
        return ResponseEntity.ok(new ApiResponse<>(200, "User ID: " + id, "User information"));
    }
}
