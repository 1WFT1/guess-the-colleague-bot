package com.wft.guesscolleague.dto;

import com.wft.guesscolleague.model.TelegramUser;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsDTO {
    private Long telegramId;
    private String fullName;
    private String username;
    private String firstName;
    private String lastName;
    private int totalScore;
    private int gamesPlayed;
    private int correctAnswers;
    private int wrongAnswers;
    private int bestStreak;
    private int currentStreak;
    private boolean isAdmin;
    private boolean isActive;
    private String lastActive;

    public UserStatsDTO(TelegramUser user) {
        this.telegramId = user.getTelegramId();
        this.fullName = user.getFullName();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.totalScore = user.getTotalScore();
        this.gamesPlayed = user.getGamesPlayed();
        this.isAdmin = user.isAdmin();
        this.isActive = user.isActive();
        this.lastActive = user.getLastActive() != null ? user.getLastActive().toString() : null;
        this.correctAnswers = 0;
        this.wrongAnswers = 0;
        this.bestStreak = 0;
        this.currentStreak = 0;
    }
}