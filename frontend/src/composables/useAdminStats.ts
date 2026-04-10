/**
 * Хук для управления статистикой в админ-панели
 * Загружает данные из бэкенда и вычисляет агрегированные показатели
 */

import { ref, onMounted } from 'vue';
import type { Employee } from '../types/game';
import gameApi from '../api/game';

// Структура статистики для отображения в админ-панели
interface GameStats {
  totalPlayers: number;      // Всего зарегистрированных игроков
  activeToday: number;       // Активных за последние 24 часа
  totalQuestions: number;    // Всего отвеченных вопросов
  averageScore: number;      // Средний балл среди всех игроков
  totalGames: number;        // Всего сыгранных игр (сессий)
  topPlayer: {               // Лучший игрок по очкам
    name: string;
    score: number;
  } | null;
}

// Статистика отдельного пользователя (получаем из API)
interface UserStats {
  telegramId: number;
  fullName: string;
  totalScore: number;
  gamesPlayed: number;
  correctAnswers: number;
  wrongAnswers: number;
  currentStreak: number;
  bestStreak: number;
  isActive: boolean;
  lastActive: string;
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

  /**
   * Получение всех пользователей из бэкенда
   */
  const getAllUsers = async (): Promise<UserStats[]> => {
    try {
      const response = await gameApi.getAllUsers();
      return response;
    } catch (err) {
      console.error('Error fetching users:', err);
      return [];
    }
  };

  /**
   * Подсчет активных пользователей за последние 24 часа
   * Сравнивает lastActive с текущей датой
   */
  const getActiveToday = (users: UserStats[]): number => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    const active = users.filter(user => {
      if (!user.lastActive) return false;
      const lastActive = new Date(user.lastActive);
      return lastActive >= today;
    });
    
    return active.length;
  };

  /**
   * Подсчет общего количества отвеченных вопросов
   * Суммирует correctAnswers + wrongAnswers по всем пользователям
   */
  const getTotalQuestions = (users: UserStats[]): number => {
    return users.reduce((sum, user) => sum + (user.correctAnswers + user.wrongAnswers), 0);
  };

  /**
   * Подсчет среднего балла среди всех игроков
   */
  const getAverageScore = (users: UserStats[]): number => {
    if (users.length === 0) return 0;
    const totalScore = users.reduce((sum, user) => sum + user.totalScore, 0);
    return Math.round(totalScore / users.length);
  };

  /**
   * Подсчет общего количества сыгранных игр (сессий)
   */
  const getTotalGames = (users: UserStats[]): number => {
    return users.reduce((sum, user) => sum + user.gamesPlayed, 0);
  };

  /**
   * Поиск лучшего игрока по общему количеству очков
   */
  const getTopPlayer = (users: UserStats[]): { name: string; score: number } | null => {
    if (users.length === 0) return null;
    
    const best = users.reduce((prev, current) => 
      prev.totalScore > current.totalScore ? prev : current, users[0]);
    
    return {
      name: best.fullName,
      score: best.totalScore
    };
  };

  /**
   * Обновление всей статистики из бэкенда
   * Вызывается при загрузке админ-панели и при нажатии кнопки "Обновить"
   */
  const updateStats = async (employees: Employee[] = []) => {
    isLoading.value = true;
    error.value = null;
    
    try {
      const users = await getAllUsers();
      
      stats.value = {
        totalPlayers: users.length,
        activeToday: getActiveToday(users),
        totalQuestions: getTotalQuestions(users),
        averageScore: getAverageScore(users),
        totalGames: getTotalGames(users),
        topPlayer: getTopPlayer(users)
      };
      
      console.log('Stats updated from backend:', stats.value);
    } catch (err) {
      console.error('Error updating stats:', err);
      error.value = 'Ошибка загрузки статистики';
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * Обновление статистики из списка сотрудников
   * Вызывается после загрузки/изменения сотрудников
   */
  const updateFromEmployees = (employees: Employee[]) => {
    updateStats(employees);
  };

  /**
   * Принудительное обновление статистики
   */
  const refresh = () => {
    updateStats();
  };

  // Инициализация при монтировании компонента
  onMounted(() => {
    updateStats();
  });

  return {
    stats,           // Реактивные данные статистики
    isLoading,       // Флаг загрузки
    error,           // Ошибка при загрузке
    updateFromEmployees,
    refresh
  };
}