import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import gameApi from '../api/game';
import type { Question, AnswerResponse } from '../types/game';

export const useGameStore = defineStore('game', () => {
  const getWeekDay = (): string => {
    const days = ['Вс', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб'];
    const today = new Date().getDay();
    const day = days[today];
    return day || 'Пн';
  };



  // State
  const sessionId = ref<string | null>(null);
  const userId = ref<number | null>(null);
  const currentQuestion = ref<Question | null>(null);
  const score = ref(0);
  const correctCount = ref(0);
  const wrongCount = ref(0);
  const isLoading = ref(false);
  const feedback = ref<any>(null);
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

  // Загрузка сохраненной статистики
  const loadSavedStats = () => {
    const savedStats = localStorage.getItem('gameStats');
    if (savedStats) {
      try {
        const stats = JSON.parse(savedStats);
        score.value = stats.score || 0;
        correctCount.value = stats.correctCount || 0;
        wrongCount.value = stats.wrongCount || 0;
        bestStreak.value = stats.bestStreak || 0;
        currentStreak.value = stats.currentStreak || 0;
        console.log('Loaded saved stats:', stats);
        return true;
      } catch (e) {
        console.error('Failed to load stats:', e);
      }
    }
    return false;
  };

  // Сохранение статистики
  const saveStats = () => {
    const stats = {
      score: score.value,
      correctCount: correctCount.value,
      wrongCount: wrongCount.value,
      bestStreak: bestStreak.value,
      currentStreak: currentStreak.value,
      lastUpdated: new Date().toISOString()
    };
    localStorage.setItem('gameStats', JSON.stringify(stats));
    
    // Сохраняем недельную статистику
    const today = getWeekDay();
    const weeklyStats = getWeeklyStats();
    weeklyStats[today] = score.value;
    localStorage.setItem('weeklyStats', JSON.stringify(weeklyStats));
    
    console.log('Saved stats:', stats);
    console.log('Saved weekly stats:', weeklyStats);
  };

  const getWeeklyStats = (): Record<string, number> => {
    const saved = localStorage.getItem('weeklyStats');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (e) {
        console.error('Failed to parse weekly stats', e);
      }
    }
    return {
      'Пн': 0,
      'Вт': 0,
      'Ср': 0,
      'Чт': 0,
      'Пт': 0,
      'Сб': 0,
      'Вс': 0
    };
  };

  const initGame = async (telegramUserId: number, telegramChatId?: number) => {
    try {
      isLoading.value = true;
      error.value = null;
      userId.value = telegramUserId;
      
      // Загружаем сохраненную статистику
      loadSavedStats();
      console.log('Loaded stats on game init:', { score: score.value, correct: correctCount.value });
      
      // Создаем новую сессию
      const id = await gameApi.createSession(telegramUserId, telegramChatId || telegramUserId);
      sessionId.value = id;
      console.log('Session created:', id);
      
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
      console.log('New question loaded:', currentQuestion.value);
    } catch (err) {
      error.value = 'Не удалось загрузить вопрос. Попробуйте позже.';
      console.error('Load question error:', err);
    } finally {
      isLoading.value = false;
    }
  };

  const submitAnswer = async (selectedIndex: number) => {
    if (!sessionId.value || !currentQuestion.value) {
      console.error('No session or question');
      return;
    }

    console.log('=== SUBMITTING ANSWER ===');
    console.log('Selected index:', selectedIndex);
    console.log('Selected option:', currentQuestion.value.options[selectedIndex]);

    try {
      isLoading.value = true;
      
      const result = await gameApi.submitAnswer(
        sessionId.value,
        currentQuestion.value.questionId,
        selectedIndex
      );
      
      console.log('Answer result:', result);
      
      const isAnswerCorrect = result.correct === true;
      
      // Обновляем счет
      score.value = result.newTotalScore;
      
      if (isAnswerCorrect) {
        correctCount.value++;
        currentStreak.value++;
        if (currentStreak.value > bestStreak.value) {
          bestStreak.value = currentStreak.value;
        }
        console.log('✅ Correct answer! Streak:', currentStreak.value);
      } else {
        wrongCount.value++;
        currentStreak.value = 0;
        console.log('❌ Wrong answer. Correct is:', result.correctAnswer);
      }
      
      // Сохраняем статистику
      saveStats();
      
      feedback.value = {
        isCorrect: isAnswerCorrect,
        pointsDelta: result.pointsDelta,
        newTotalScore: result.newTotalScore,
        correctAnswer: result.correctAnswer,
        message: result.message
      };
      
      // Загружаем следующий вопрос через 2 секунды
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
    console.log('Game reset, stats preserved:', { score: score.value, correct: correctCount.value });
  };

  // Загружаем статистику при создании стора
  loadSavedStats();

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
    
    // Actions
    initGame,
    loadNextQuestion,
    submitAnswer,
    resetGame,
    loadSavedStats,
  };
});