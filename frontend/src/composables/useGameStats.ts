// composables/useGameStats.ts
import { ref, computed, watch } from 'vue';
import type { Employee } from '../types/game';

export function useGameStats() {
  const totalPlayers = ref(0);
  const activeToday = ref(0);
  const totalQuestions = ref(0);
  const averageScore = ref(0);
  const totalGamesPlayed = ref(0);

  // Функция для получения количества вопросов из localStorage
  const loadTotalQuestions = (): number => {
    try {
      // Вариант 1: из общей статистики
      const allPlayersStats = localStorage.getItem('guess_colleague_all_players_v1');
      if (allPlayersStats) {
        const players = JSON.parse(allPlayersStats);
        let total = 0;
        players.forEach((player: any) => {
          total += (player.correctCount || 0) + (player.wrongCount || 0);
        });
        return total;
      }
      
      // Вариант 2: из статистики текущего игрока
      const currentStats = localStorage.getItem('gameStats');
      if (currentStats) {
        const stats = JSON.parse(currentStats);
        return (stats.correctCount || 0) + (stats.wrongCount || 0);
      }
    } catch (e) {
      console.error('Failed to load total questions:', e);
    }
    return 0;
  };

  // Функция для получения среднего балла
  const calculateAverageScore = (): number => {
    try {
      const allPlayersStats = localStorage.getItem('guess_colleague_all_players_v1');
      if (allPlayersStats) {
        const players = JSON.parse(allPlayersStats);
        if (players.length === 0) return 0;
        
        let totalScore = 0;
        players.forEach((player: any) => {
          totalScore += player.totalScore || 0;
        });
        return Math.round(totalScore / players.length);
      }
      
      // Если нет других игроков, берем текущего
      const currentStats = localStorage.getItem('gameStats');
      if (currentStats) {
        const stats = JSON.parse(currentStats);
        return stats.score || 0;
      }
    } catch (e) {
      console.error('Failed to calculate average score:', e);
    }
    return 0;
  };

  // Функция для получения активных сегодня пользователей
  const getActiveTodayCount = (): number => {
    try {
      const today = new Date().toISOString().split('T')[0];
      const dailyStats = localStorage.getItem(`daily_stats_${today}`);
      if (dailyStats) {
        const stats = JSON.parse(dailyStats);
        return stats.activeUsers || 0;
      }
    } catch (e) {
      console.error('Failed to get active today count:', e);
    }
    return 0;
  };

  // Обновление всей статистики
  const updateAllStats = () => {
    totalQuestions.value = loadTotalQuestions();
    averageScore.value = calculateAverageScore();
    activeToday.value = getActiveTodayCount();
  };

  // Обновление из списка сотрудников
  const updateFromEmployees = (employees: Employee[]) => {
    totalPlayers.value = employees.length;
    updateAllStats(); // Также обновляем остальную статистику
  };

  // Подписка на изменения в localStorage
  const startListening = () => {
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key?.includes('game_stats') || 
          e.key?.includes('all_players') || 
          e.key?.includes('daily_stats')) {
        updateAllStats();
      }
    };
    
    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  };

  // Вычисляемые свойства для удобства
  const stats = computed(() => ({
    totalPlayers: totalPlayers.value,
    activeToday: activeToday.value,
    totalQuestions: totalQuestions.value,
    averageScore: averageScore.value,
    totalGamesPlayed: totalGamesPlayed.value
  }));

  // Инициализация
  const init = () => {
    updateAllStats();
    return startListening();
  };

  // Принудительное обновление
  const refresh = () => {
    updateAllStats();
  };

  return {
    // Состояние
    totalPlayers,
    activeToday,
    totalQuestions,
    averageScore,
    totalGamesPlayed,
    
    // Вычисляемые
    stats,
    
    // Методы
    updateFromEmployees,
    updateAllStats,
    refresh,
    init
  };
}