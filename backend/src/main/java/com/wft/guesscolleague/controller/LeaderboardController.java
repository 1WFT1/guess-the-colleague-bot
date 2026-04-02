package com.wft.guesscolleague.controller;

import com.wft.guesscolleague.dto.LeaderboardDTO;
import com.wft.guesscolleague.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public ResponseEntity<LeaderboardDTO> getLeaderboard(@RequestParam String week,
                                                         @RequestParam(required = false) Long userId) {
        LeaderboardDTO leaderboard = leaderboardService.getLeaderboard(week, userId);
        return ResponseEntity.ok(leaderboard);
    }
}