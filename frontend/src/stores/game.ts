import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import gameApi from '../api/game';
import { saveTodayStats, loadCurrentWeekStats, getWeekKey, getWeekDay, WeekDay } from '../utils/weeklyStats';
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
  
  // Общая сумма очков за неделю
  const totalWeeklyScore = computed(() => {
    const weeklyStats = getCurrentWeeklyStats();
    if (!weeklyStats) return score.value;
    const days: WeekDay[] = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
    const total = days.reduce((sum, day) => sum + (weeklyStats[day] || 0), 0);
    return total > 0 ? total : score.value;
  });

  // Загрузка статистики с бэкенда
  const loadStatsFromBackend = async () => {
    if (!userId.value) return false;
    
    try {
      const stats = await gameApi.getUserStats(userId.value);
      console.log('[Backend] Loaded user stats:', stats);
      
      score.value = stats.totalScore || 0;
      // correctCount и wrongCount пока не хранятся в БД, оставляем локально
      // Но для синхронизации можно добавить поля в БД
      
      return true;
    } catch (err) {
      console.error('[Backend] Failed to load stats:', err);
      return false;
    }
  };

  // Сохранение статистики на бэкенд
  const saveStatsToBackend = async () => {
    if (!userId.value) return;
    
    try {
      await gameApi.updateUserStats(userId.value, {
        score: score.value,
        correctCount: correctCount.value,
        wrongCount: wrongCount.value,
        currentStreak: currentStreak.value,
        bestStreak: bestStreak.value
      });
      console.log('[Backend] Saved stats for user:', userId.value);
    } catch (err) {
      console.error('[Backend] Failed to save stats:', err);
    }
  };

  // Сохранение в localStorage (только для недельной статистики)
  const saveStats = () => {
    if (!userId.value) return;
    
    // Сохраняем недельную статистику в localStorage
    saveTodayStats(score.value);
    
    // Сохраняем в бэкенд
    saveStatsToBackend();
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

  const recordDailyActivity = () => {
    const today = new Date().toISOString().split('T')[0];
    const key = `daily_stats_${today}`;
    
    let dailyStats: any = { activeUsers: 0, totalGames: 0 };
    const stored = localStorage.getItem(key);
    
    if (stored) {
      try {
        dailyStats = JSON.parse(stored);
      } catch (e) {}
    }
    
    const userKey = `user_${userId.value}`;
    if (!dailyStats[userKey]) {
      dailyStats[userKey] = true;
      dailyStats.activeUsers++;
    }
    
    dailyStats.totalGames++;
    localStorage.setItem(key, JSON.stringify(dailyStats));
  };

  // Public methods
  const initGame = async (telegramUserId: number, telegramChatId?: number) => {
    try {
      isLoading.value = true;
      error.value = null;
      userId.value = telegramUserId;
      
      // Загружаем статистику с бэкенда
      await loadStatsFromBackend();
      
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
      
      // Сохраняем статистику (в бэкенд и localStorage для недельной)
      saveStats();
      
      feedback.value = result;
      
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

  const resetStats = async () => {
    console.log('[Reset] Starting full stats reset...');
    
    // Обнуляем все счета
    score.value = 0;
    correctCount.value = 0;
    wrongCount.value = 0;
    currentStreak.value = 0;
    bestStreak.value = 0;
    
    // Сохраняем обнуленную статистику на бэкенд
    await saveStatsToBackend();
    
    // Обнуляем недельную статистику в localStorage
    const weekKey = getWeekKey();
    const emptyStats = {
      Пн: 0, Вт: 0, Ср: 0, Чт: 0, Пт: 0, Сб: 0, Вс: 0, totalScore: 0
    };
    localStorage.setItem(weekKey, JSON.stringify(emptyStats));
    
    console.log('[Reset] All game stats reset to zero');
    
    // Перезагружаем страницу
    setTimeout(() => {
      window.location.reload();
    }, 500);
  };

  const updateStatsFromBackend = (backendStats: any) => {
    if (backendStats) {
      score.value = backendStats.totalScore || 0;
      // Если бэкенд возвращает дополнительную статистику
      if (backendStats.correctAnswers !== undefined) {
        correctCount.value = backendStats.correctAnswers;
      }
      if (backendStats.wrongAnswers !== undefined) {
        wrongCount.value = backendStats.wrongAnswers;
      }
      if (backendStats.bestStreak !== undefined) {
        bestStreak.value = backendStats.bestStreak;
      }
      console.log('[Store] Updated stats from backend:', {
        score: score.value,
        correct: correctCount.value,
        wrong: wrongCount.value
      });
    }
  };

  const getCurrentWeeklyStats = () => {
    return loadCurrentWeekStats();
  };

  return {
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
    totalWeeklyScore,
    accuracy,
    totalQuestions,
    initGame,
    loadNextQuestion,
    submitAnswer,
    resetGame,
    resetStats,
    getCurrentWeeklyStats,
    loadStatsFromBackend,
    saveStatsToBackend,
    updateStatsFromBackend
  };
});