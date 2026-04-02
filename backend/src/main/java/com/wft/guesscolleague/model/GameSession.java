package com.wft.guesscolleague.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "game_sessions")
@Data
@NoArgsConstructor
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "total_score")
    private int totalScore = 0;

    @Column(name = "correct_answers")
    private int correctAnswers = 0;

    @Column(name = "wrong_answers")
    private int wrongAnswers = 0;

    @Column(name = "is_active")
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    @UpdateTimestamp
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    public void addScore(int points) {
        this.totalScore = Math.max(0, this.totalScore + points);
    }

    public void incrementCorrect() {
        this.correctAnswers++;
    }

    public void incrementWrong() {
        this.wrongAnswers++;
    }
}