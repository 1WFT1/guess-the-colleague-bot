package com.wft.guesscolleague.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {
    private UUID sessionId;
    private UUID questionId;
    private int selectedOptionIndex;
}