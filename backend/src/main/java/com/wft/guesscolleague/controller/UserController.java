package com.wft.guesscolleague.controller;

import com.wft.guesscolleague.model.TelegramUser;
import com.wft.guesscolleague.service.TelegramUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final TelegramUserService userService;

    @GetMapping("/{telegramId}")
    public ResponseEntity<TelegramUser> getUser(@PathVariable Long telegramId) {
        return ResponseEntity.ok(userService.getUserStats(telegramId));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<TelegramUser>> getLeaderboard() {
        return ResponseEntity.ok(userService.getLeaderboard());
    }

    @GetMapping("/active/count")
    public ResponseEntity<Long> getActiveUsersCount() {
        return ResponseEntity.ok(userService.getActiveUsersCount());
    }

    @GetMapping("/all")
    public ResponseEntity<List<TelegramUser>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}