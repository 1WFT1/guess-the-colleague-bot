import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import gameApi from '../api/game';
import type { Question, AnswerResponse } from '../types/game';

export const useGameStore = defineStore('game', () => {
  type WeekDay = 'Пн' | 'Вт' | 'Ср' | 'Чт' | 'Пт' | 'Сб' | 'Вс';
  const getWeekDay = (): WeekDay => {
    const now = new Date();
    const days: WeekDay[] = ['Вс', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб'];
    const dayIndex = now.getDay();
    
    // Используем утверждение типа, так как dayIndex всегда 0-6
    return days[dayIndex] as WeekDay;
  };

  // Тип для недельной статистики
  interface WeeklyStatsType {
    Пн: number;
    Вт: number;
    Ср: number;
    Чт: number;
    Пт: number;
    Сб: number;
    Вс: number;
    weekKey?: string;
    totalScore?: number;
  }

  const getWeekStart = (): Date => {
    const now = new Date();
    const day = now.getDay();
    const diff = day === 0 ? 6 : day - 1;
    const monday = new Date(now);
    monday.setDate(now.getDate() - diff);
    monday.setHours(0, 0, 0, 0);
    return monday;
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
    
    // Обновляем общую статистику для админ-панели
    updateGlobalStats();
    
    // Сохраняем недельную статистику
    saveWeeklyStats(score.value);
  };

  const updateGlobalStats = () => {
    // Получаем всех игроков
    let allPlayers: any[] = [];
    const savedPlayers = localStorage.getItem('allPlayersStats');
    if (savedPlayers) {
      allPlayers = JSON.parse(savedPlayers);
    }
    
    // Обновляем или добавляем текущего игрока
    const currentUserId = localStorage.getItem('currentUserId');
    const currentStats = {
      userId: currentUserId,
      totalScore: score.value,
      correctCount: correctCount.value,
      wrongCount: wrongCount.value,
      lastUpdated: new Date().toISOString()
    };
    
    const existingIndex = allPlayers.findIndex(p => p.userId === currentUserId);
    if (existingIndex !== -1) {
      allPlayers[existingIndex] = currentStats;
    } else {
      allPlayers.push(currentStats);
    }
    
    localStorage.setItem('allPlayersStats', JSON.stringify(allPlayers));
    
    // Обновляем общее количество вопросов
    const totalQuestions = correctCount.value + wrongCount.value;
    localStorage.setItem('totalQuestions', String(totalQuestions));
  };

  interface WeeklyStatsType {
    Пн: number;
    Вт: number;
    Ср: number;
    Чт: number;
    Пт: number;
    Сб: number;
    Вс: number;
    weekKey?: string;
    totalScore?: number;
  }

  const getWeeklyStats = (): WeeklyStatsType => {
    const weekStart = getWeekStart();
    const weekKey = weekStart.toISOString().split('T')[0];
    
    const saved = localStorage.getItem(`weeklyStats_${weekKey}`);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        return {
          Пн: parsed.Пн ?? 0,
          Вт: parsed.Вт ?? 0,
          Ср: parsed.Ср ?? 0,
          Чт: parsed.Чт ?? 0,
          Пт: parsed.Пт ?? 0,
          Сб: parsed.Сб ?? 0,
          Вс: parsed.Вс ?? 0,
          weekKey: weekKey,
          totalScore: parsed.totalScore ?? 0
        };
      } catch (e) {
        console.error('Failed to parse weekly stats', e);
      }
    }
    
    return {
      Пн: 0, Вт: 0, Ср: 0, Чт: 0, Пт: 0, Сб: 0, Вс: 0,
      weekKey: weekKey,
      totalScore: 0
    };
  };

  // Сохранение недельной статистики
  const saveWeeklyStats = (currentScore: number): void => {
    const weekStart = getWeekStart();
    const weekKey = weekStart.toISOString().split('T')[0];
    const today = getWeekDay();
    
    const weeklyStats = getWeeklyStats();
    
    // Обновляем только сегодняшний день - используем проверку через if
    if (today === 'Пн') weeklyStats.Пн = currentScore;
    else if (today === 'Вт') weeklyStats.Вт = currentScore;
    else if (today === 'Ср') weeklyStats.Ср = currentScore;
    else if (today === 'Чт') weeklyStats.Чт = currentScore;
    else if (today === 'Пт') weeklyStats.Пт = currentScore;
    else if (today === 'Сб') weeklyStats.Сб = currentScore;
    else if (today === 'Вс') weeklyStats.Вс = currentScore;
    
    weeklyStats.weekKey = weekKey;
    
    // Обновляем общий счет за неделю
    const days: WeekDay[] = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
    weeklyStats.totalScore = days.reduce((sum, day) => sum + (weeklyStats[day] || 0), 0);
    
    localStorage.setItem(`weeklyStats_${weekKey}`, JSON.stringify(weeklyStats));
    console.log('Saved weekly stats:', weeklyStats);
  };

  // Функция для получения текущей недельной статистики для компонентов
  const getCurrentWeeklyStats = (): Omit<WeeklyStatsType, 'weekKey' | 'totalScore'> => {
    const { weekKey, totalScore, ...stats } = getWeeklyStats();
    return stats;
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

    getCurrentWeeklyStats,
    
    // Actions
    initGame,
    loadNextQuestion,
    submitAnswer,
    resetGame,
    loadSavedStats,
  };
});