// composables/useLeaderboard.ts
import { ref } from 'vue';

interface LeaderboardPlayer {
  userId: number;
  fullName: string;
  totalScore: number;
  accuracy: number;
  gamesPlayed?: number;
  rank?: number;
  correctCount?: number;
  wrongCount?: number;
  totalQuestions?: number;
}

export function useLeaderboard() {
  const globalLeaderboard = ref<LeaderboardPlayer[]>([]);
  const weeklyLeaderboard = ref<LeaderboardPlayer[]>([]);
  const isLoading = ref(false);

  const loadGlobalLeaderboard = (): LeaderboardPlayer[] => {
    const players: LeaderboardPlayer[] = [];
    const allPlayersKey = 'guess_colleague_all_players_v1';
    const saved = localStorage.getItem(allPlayersKey);
    
    if (saved) {
      try {
        const allPlayers = JSON.parse(saved);
        // Group by userId and sum scores
        const userMap = new Map<number, LeaderboardPlayer>();
        
        allPlayers.forEach((player: any) => {
          const existing = userMap.get(player.userId);
          if (existing) {
            existing.totalScore += player.totalScore;
            existing.gamesPlayed = (existing.gamesPlayed || 1) + 1;
            // Обновляем счетчики вопросов
            existing.correctCount = (existing.correctCount || 0) + (player.correctCount || 0);
            existing.wrongCount = (existing.wrongCount || 0) + (player.wrongCount || 0);
            existing.totalQuestions = (existing.totalQuestions || 0) + (player.correctCount || 0) + (player.wrongCount || 0);
            // Recalculate accuracy
            const total = (existing.totalQuestions || 1);
            existing.accuracy = Math.round(((existing.correctCount || 0) / total) * 100);
          } else {
            const correctCount = player.correctCount || 0;
            const wrongCount = player.wrongCount || 0;
            const totalQuestions = correctCount + wrongCount;
            userMap.set(player.userId, {
              userId: player.userId,
              fullName: player.fullName || `Игрок ${player.userId}`,
              totalScore: player.totalScore,
              accuracy: player.accuracy || 0,
              gamesPlayed: 1,
              correctCount: correctCount,
              wrongCount: wrongCount,
              totalQuestions: totalQuestions
            });
          }
        });
        
        const playersList = Array.from(userMap.values());
        playersList.sort((a, b) => b.totalScore - a.totalScore);
        
        return playersList.map((p, idx) => ({ ...p, rank: idx + 1 }));
      } catch (e) {
        console.error('Failed to load global leaderboard', e);
      }
    }
    return [];
  };

  const loadWeeklyLeaderboard = (): LeaderboardPlayer[] => {
    const weekKey = getCurrentWeekKey();
    const weeklyStatsKey = `weekly_stats_${weekKey}`;
    const saved = localStorage.getItem(weeklyStatsKey);
    
    if (saved) {
      try {
        const stats = JSON.parse(saved);
        if (stats.players) {
          return stats.players
            .sort((a: LeaderboardPlayer, b: LeaderboardPlayer) => b.totalScore - a.totalScore)
            .map((p: LeaderboardPlayer, idx: number) => ({ ...p, rank: idx + 1 }));
        }
      } catch (e) {
        console.error('Failed to load weekly leaderboard', e);
      }
    }
    return [];
  };

  const getCurrentWeekKey = (): string => {
    const now = new Date();
    const year = now.getFullYear();
    const weekNumber = getWeekNumber(now);
    return `${year}_${weekNumber}`;
  };

  const getWeekNumber = (date: Date): number => {
    const firstDayOfYear = new Date(date.getFullYear(), 0, 1);
    const pastDaysOfYear = (date.getTime() - firstDayOfYear.getTime()) / 86400000;
    return Math.ceil((pastDaysOfYear + firstDayOfYear.getDay() + 1) / 7);
  };

  const loadLeaderboardData = () => {
    isLoading.value = true;
    globalLeaderboard.value = loadGlobalLeaderboard();
    weeklyLeaderboard.value = loadWeeklyLeaderboard();
    isLoading.value = false;
  };

  const getGlobalLeaderboard = () => globalLeaderboard.value;
  const getWeeklyLeaderboard = () => weeklyLeaderboard.value;

  const getUserRank = (userId: number): number => {
    const index = globalLeaderboard.value.findIndex(p => p.userId === userId);
    return index !== -1 ? index + 1 : globalLeaderboard.value.length + 1;
  };

  return {
    globalLeaderboard,
    weeklyLeaderboard,
    isLoading,
    loadLeaderboardData,
    getGlobalLeaderboard,
    getWeeklyLeaderboard,
    getUserRank
  };
}