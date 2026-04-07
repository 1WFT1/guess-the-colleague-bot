import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import gameApi from '../api/game';
import { saveTodayStats, loadCurrentWeekStats, getWeekKey, getWeekDay } from '../utils/weeklyStats';
import type { Question, AnswerResponse, GameStats } from '../types/game';

export const useGameStore = defineStore('game', () => {
  // State
  const sessionId = ref<string | null>(null);
  const userId = ref<number | null>(null);
  const currentQuestion = ref<Question | null>(null);
  const score = ref(0);
  const correctCount = ref(0);
  const wrongCount = ref(0);
  const isLoading = ref(false);
  const feedback = ref<AnswerResponse | null>(null);
  const isGameActive = ref(true);
  const error = ref<string | null>(null);
  const currentStreak = ref(0);
  const bestStreak = ref(0);

  // Getters
  const accuracy = computed(() => {
    const total = correctCount.value + wrongCount.value;
    if (total === 0) return 0;
    return Math.round((correctCount.value / total) * 100);
  });

  const totalQuestions = computed(() => correctCount.value + wrongCount.value);

  // Private methods
  const loadSavedStats = (): boolean => {
    const savedUserId = localStorage.getItem('currentUserId');
    const currentUserId = savedUserId || userId.value;
    if (!currentUserId) return false;
    
    const key = `game_stats_${currentUserId}`;
    const savedStats = localStorage.getItem(key);
    
    if (savedStats) {
      try {
        const stats: GameStats = JSON.parse(savedStats);
        score.value = stats.score;
        correctCount.value = stats.correctCount;
        wrongCount.value = stats.wrongCount;
        bestStreak.value = stats.bestStreak;
        currentStreak.value = stats.currentStreak;
        return true;
      } catch (e) {
        console.error('Failed to load stats:', e);
      }
    }
    
    // Если нет сохраненной статистики, но есть недельная - восстановить
    if (userId.value) {
      const weeklyStats = loadCurrentWeekStats();
      const today = getWeekDay();
      const weeklyScore = weeklyStats[today] || 0;
      
      if (weeklyScore > 0) {
        console.log('[loadSavedStats] No saved stats, restoring from weekly:', weeklyScore);
        score.value = weeklyScore;
        saveStats();
        return true;
      }
    }
    
    return false;
  };

  const saveStats = () => {
    if (!userId.value) return;
    
    const stats: GameStats = {
      score: score.value,
      correctCount: correctCount.value,
      wrongCount: wrongCount.value,
      bestStreak: bestStreak.value,
      currentStreak: currentStreak.value,
      lastUpdated: new Date().toISOString()
    };
    
    localStorage.setItem(`game_stats_${userId.value}`, JSON.stringify(stats));
    localStorage.setItem('currentUserId', String(userId.value));
    
    // Используем единую функцию для сохранения недельной статистики
    saveTodayStats(score.value);
    
    // Update global players list
    updateGlobalPlayersList();
  };

  const updateGlobalPlayersList = () => {
    const allPlayersKey = 'guess_colleague_all_players_v1';
    let allPlayers: any[] = [];
    const saved = localStorage.getItem(allPlayersKey);
    
    if (saved) {
      try {
        allPlayers = JSON.parse(saved);
      } catch (e) {}
    }
    
    const currentPlayer = {
      userId: userId.value,
      fullName: localStorage.getItem('userName') || `Игрок ${userId.value}`,
      totalScore: score.value,
      correctCount: correctCount.value,
      wrongCount: wrongCount.value,
      accuracy: accuracy.value,
      lastUpdated: new Date().toISOString()
    };
    
    const existingIndex = allPlayers.findIndex(p => p.userId === userId.value);
    if (existingIndex !== -1) {
      allPlayers[existingIndex] = currentPlayer;
    } else {
      allPlayers.push(currentPlayer);
    }
    
    localStorage.setItem(allPlayersKey, JSON.stringify(allPlayers));
  };

  const updateStreak = (isCorrect: boolean) => {
    if (isCorrect) {
      currentStreak.value++;
      if (currentStreak.value > bestStreak.value) {
        bestStreak.value = currentStreak.value;
      }
    } else {
      currentStreak.value = 0;
    }
  };

  interface DailyStats {
    activeUsers: number;
    totalGames: number;
    [key: string]: number | boolean;
  }

  const recordDailyActivity = () => {
    const today = new Date().toISOString().split('T')[0];
    const key = `daily_stats_${today}`;
    
    let dailyStats: DailyStats = { activeUsers: 0, totalGames: 0 };
    const stored = localStorage.getItem(key);
    
    if (stored) {
      try {
        const parsed = JSON.parse(stored);
        dailyStats = {
          activeUsers: parsed.activeUsers || 0,
          totalGames: parsed.totalGames || 0,
          ...parsed
        };
      } catch (e) {}
    }
    
    const userKey = `user_${userId.value}`;
    if (!dailyStats[userKey]) {
      dailyStats[userKey] = true;
      dailyStats.activeUsers++;
    }
    
    dailyStats.totalGames++;
    localStorage.setItem(key, JSON.stringify(dailyStats));
    
    console.log('Daily activity recorded:', dailyStats);
  };

  // Public methods
  const initGame = async (telegramUserId: number, telegramChatId?: number) => {
    try {
      isLoading.value = true;
      error.value = null;
      userId.value = telegramUserId;
      
      loadSavedStats();
      recordDailyActivity();
      
      const id = await gameApi.createSession(telegramUserId, telegramChatId || telegramUserId);
      sessionId.value = id;
      
      await loadNextQuestion();
    } catch (err) {
      error.value = 'Не удалось создать игровую сессию. Проверьте подключение к серверу.';
      console.error('Init game error:', err);
    } finally {
      isLoading.value = false;
    }
  };

  const loadNextQuestion = async () => {
    if (!sessionId.value) return;

    try {
      isLoading.value = true;
      feedback.value = null;
      error.value = null;
      
      const question = await gameApi.getNextQuestion(sessionId.value);
      currentQuestion.value = question;
    } catch (err) {
      error.value = 'Не удалось загрузить вопрос. Попробуйте позже.';
      console.error('Load question error:', err);
    } finally {
      isLoading.value = false;
    }
  };

  const submitAnswer = async (selectedIndex: number) => {
    if (!sessionId.value || !currentQuestion.value) return;

    try {
      isLoading.value = true;
      
      const result = await gameApi.submitAnswer(
        sessionId.value,
        currentQuestion.value.questionId,
        selectedIndex
      );
      
      // Update game state
      score.value = result.newTotalScore;
      updateStreak(result.correct);
      
      if (result.correct) {
        correctCount.value++;
      } else {
        wrongCount.value++;
      }
      
      saveStats();
      
      feedback.value = result;
      
      // Load next question after delay
      setTimeout(() => {
        loadNextQuestion();
      }, 2000);
      
    } catch (err) {
      console.error('Submit answer error:', err);
      error.value = 'Не удалось отправить ответ. Проверьте подключение.';
    } finally {
      isLoading.value = false;
    }
  };

  const resetGame = () => {
    sessionId.value = null;
    currentQuestion.value = null;
    feedback.value = null;
    error.value = null;
    isGameActive.value = true;
  };

  const resetStats = () => {
    // Обнуляем все счета
    score.value = 0;
    correctCount.value = 0;
    wrongCount.value = 0;
    currentStreak.value = 0;
    bestStreak.value = 0;
    
    // Сохраняем обнуленную статистику
    saveStats();
    
    // Обнуляем недельную статистику за сегодня
    const today = getWeekDay();
    const weekKey = getWeekKey();
    const existingStats = localStorage.getItem(weekKey);
    
    if (existingStats) {
      try {
        const stats = JSON.parse(existingStats);
        stats[today] = 0;
        
        // Пересчитываем totalScore
        const days = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
        stats.totalScore = days.reduce((sum: number, day: string) => sum + (stats[day] || 0), 0);
        
        localStorage.setItem(weekKey, JSON.stringify(stats));
        console.log(`[Reset] Weekly stats cleared for ${today}`);
      } catch (e) {
        console.error('[Reset] Failed to clear weekly stats:', e);
      }
    }
    
    console.log('[Reset] All game stats reset to zero');
  };

  const getCurrentWeeklyStats = () => {
    return loadCurrentWeekStats();
  };

  return {
    // State
    sessionId,
    userId,
    currentQuestion,
    score,
    correctCount,
    wrongCount,
    isLoading,
    feedback,
    isGameActive,
    error,
    currentStreak,
    bestStreak,
    
    // Getters
    accuracy,
    totalQuestions,
    
    // Methods
    initGame,
    loadNextQuestion,
    submitAnswer,
    resetGame,
    resetStats,
    loadSavedStats,
    getCurrentWeeklyStats
  };
});