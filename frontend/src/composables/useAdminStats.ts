// composables/useAdminStats.ts
import { ref, onMounted, onUnmounted } from 'vue';
import type { Employee } from '../types/game';

interface GameStats {
  totalPlayers: number;
  activeToday: number;
  totalQuestions: number;
  averageScore: number;
  totalGames: number;
  topPlayer: {
    name: string;
    score: number;
  } | null;
}

export function useAdminStats() {
  const stats = ref<GameStats>({
    totalPlayers: 0,
    activeToday: 0,
    totalQuestions: 0,
    averageScore: 0,
    totalGames: 0,
    topPlayer: null
  });

  const isLoading = ref(false);
  const error = ref<string | null>(null);

  // Простая функция для получения активных сегодня
  const getActiveToday = (): number => {
    try {
      const today = new Date().toISOString().split('T')[0];
      const key = `daily_stats_${today}`;
      const stored = localStorage.getItem(key);
      
      console.log('Checking daily stats for:', key);
      console.log('Stored value:', stored);
      
      if (stored) {
        const parsed = JSON.parse(stored);
        const count = parsed.activeUsers || 0;
        console.log('Active today count:', count);
        return count;
      }
    } catch (e) {
      console.error('Error getting active today:', e);
    }
    return 0;
  };

  // Получение всех игроков
  const getTotalPlayers = (employees: Employee[]): number => {
    try {
      const uniquePlayers = new Set();
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && key.startsWith('game_stats_')) {
          const userId = key.replace('game_stats_', '');
          uniquePlayers.add(userId);
        }
      }
      return uniquePlayers.size;
    } catch (e) {
      console.error('Error getting total players:', e);
    }
    return 0;
  };

  // Получение общего количества вопросов
  const getTotalQuestions = (): number => {
    try {
      const saved = localStorage.getItem('guess_colleague_all_players_v1');
      if (saved) {
        const players = JSON.parse(saved);
        let total = 0;
        players.forEach((player: any) => {
          total += (player.correctCount || 0) + (player.wrongCount || 0);
        });
        return total;
      }
    } catch (e) {
      console.error('Error getting total questions:', e);
    }
    return 0;
  };

  // Получение среднего балла
  const getAverageScore = (): number => {
    try {
      const saved = localStorage.getItem('guess_colleague_all_players_v1');
      if (saved) {
        const players = JSON.parse(saved);
        if (players.length === 0) return 0;
        
        // Группируем по пользователям и суммируем очки
        const userScores = new Map();
        players.forEach((player: any) => {
          const existing = userScores.get(player.userId);
          if (existing) {
            userScores.set(player.userId, existing + (player.totalScore || 0));
          } else {
            userScores.set(player.userId, player.totalScore || 0);
          }
        });
        
        let total = 0;
        userScores.forEach((score) => {
          total += score;
        });
        
        return Math.round(total / userScores.size);
      }
    } catch (e) {
      console.error('Error getting average score:', e);
    }
    return 0;
  };

  // Получение лучшего игрока
  const getTopPlayer = (): { name: string; score: number } | null => {
    try {
      const saved = localStorage.getItem('guess_colleague_all_players_v1');
      if (saved) {
        const players = JSON.parse(saved);
        if (players.length === 0) return null;
        
        // Группируем по пользователям
        const userScores = new Map();
        players.forEach((player: any) => {
          const existing = userScores.get(player.userId);
          if (existing) {
            existing.score += player.totalScore;
          } else {
            userScores.set(player.userId, {
              name: player.fullName || `Игрок ${player.userId}`,
              score: player.totalScore || 0
            });
          }
        });
        
        // Находим максимальный счет
        let top = null;
        let maxScore = -1;
        userScores.forEach((player, userId) => {
          if (player.score > maxScore) {
            maxScore = player.score;
            top = player;
          }
        });
        
        return top;
      }
    } catch (e) {
      console.error('Error getting top player:', e);
    }
    return null;
  };

  // Обновление всей статистики
  const updateStats = (employees: Employee[] = []) => {
    isLoading.value = true;
    
    try {
      stats.value = {
        totalPlayers: getTotalPlayers(employees),
        activeToday: getActiveToday(),
        totalQuestions: getTotalQuestions(),
        averageScore: getAverageScore(),
        totalGames: getTotalGames(),
        topPlayer: getTopPlayer()
      };
      
      console.log('Stats updated:', stats.value);
    } catch (e) {
      console.error('Error updating stats:', e);
      error.value = 'Ошибка загрузки статистики';
    } finally {
      isLoading.value = false;
    }
  };

  const getTotalGames = (): number => {
    try {
      let total = 0;
      // Перебираем все daily_stats ключи
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && key.startsWith('daily_stats_')) {
          const value = localStorage.getItem(key);
          if (value) {getTotalPlayers 
            const stats = JSON.parse(value);
            total += stats.totalGames || 0;
          }
        }
      }
      console.log('Total games all time:', total);
      return total;
    } catch (e) {
      console.error('Failed to load total games:', e);
    }
    return 0;
  };

  // Обновление из списка сотрудников
  const updateFromEmployees = (employees: Employee[]) => {
    updateStats(employees);
  };

  // Принудительное обновление
  const refresh = () => {
    updateStats();
  };

  // Создаем тестовые данные для проверки
  const createTestData = () => {
    const today = new Date().toISOString().split('T')[0];
    const testStats = {
      activeUsers: 3,
      totalGames: 5,
      user_123: true,
      user_456: true,
      user_789: true
    };
    localStorage.setItem(`daily_stats_${today}`, JSON.stringify(testStats));
    console.log('Test data created:', testStats);
    refresh();
  };

  return {
    stats,
    isLoading,
    error,
    updateFromEmployees,
    refresh,
    createTestData // Для отладки
  };
}