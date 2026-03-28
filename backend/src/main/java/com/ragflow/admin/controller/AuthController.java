package com.ragflow.admin.controller;

import com.ragflow.admin.service.UserService;
import com.ragflow.admin.util.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Result.err("用户名和密码不能为空");
        }
        return Result.ok(userService.login(username, password));
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String nickname = body.get("nickname");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Result.err("用户名和密码不能为空");
        }
        return Result.ok(userService.register(username, password, nickname));
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        return Result.ok(userService.getCurrentUser());
    }
}
