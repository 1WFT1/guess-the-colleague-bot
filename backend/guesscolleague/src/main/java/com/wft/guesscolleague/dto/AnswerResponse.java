package com.wft.guesscolleague.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse {
    private boolean isCorrect;
    private int pointsDelta;
    private int newTotalScore;
    private String correctAnswer;
    private String message;
}