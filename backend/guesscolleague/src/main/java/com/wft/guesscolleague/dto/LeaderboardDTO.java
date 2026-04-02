package com.wft.guesscolleague.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardDTO {
    private String week;
    private List<LeaderboardEntry> entries;
    private LeaderboardEntry currentUser;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardEntry {
        private Long userId;
        private String fullName;
        private int totalScore;
        private double accuracy;
        private int rank;
    }
}