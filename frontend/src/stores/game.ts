// frontend/src/stores/game.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import gameApi from '../api/game';
import type { Question, AnswerResponse } from '../types/game';

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

  // Загрузка статистики с бэкенда
  const loadStatsFromBackend = async () => {
    if (!userId.value) return false;
    
    try {
      console.log('[Backend] Fetching stats for user:', userId.value);
      const stats = await gameApi.getUserStats(userId.value);
      console.log('[Backend] Received stats:', stats);
      
      if (stats) {
        score.value = stats.totalScore || 0;
        correctCount.value = stats.correctAnswers || 0;
        wrongCount.value = stats.wrongAnswers || 0;
        currentStreak.value = stats.currentStreak || 0;
        bestStreak.value = stats.bestStreak || 0;
        console.log('[Backend] Stats loaded into store:', {
          score: score.value,
          accuracy: accuracy.value,
          bestStreak: bestStreak.value
        });
        return true;
      }
      return false;
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
        totalScore: score.value,
        correctAnswers: correctCount.value,
        wrongAnswers: wrongCount.value,
        currentStreak: currentStreak.value,
        bestStreak: bestStreak.value
      });
      console.log('[Backend] Saved stats for user:', userId.value);
    } catch (err) {
      console.error('[Backend] Failed to save stats:', err);
    }
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

  // Public methods
  const initGame = async (telegramUserId: number, telegramChatId?: number) => {
    try {
      isLoading.value = true;
      error.value = null;
      userId.value = telegramUserId;
      
      // Загружаем статистику с бэкенда
      await loadStatsFromBackend();
      
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
      
      // Сохраняем статистику в бэкенд
      await saveStatsToBackend();
      
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
    
    // Сохраняем обнуленную статистику в бэкенд
    await saveStatsToBackend();
    
    console.log('[Reset] All game stats reset to zero');
    
    // Перезагружаем страницу
    setTimeout(() => {
      window.location.reload();
    }, 500);
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
    loadStatsFromBackend,
    saveStatsToBackend
  };
});