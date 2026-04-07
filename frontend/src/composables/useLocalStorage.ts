import { ref, watch, type Ref } from 'vue';
import type { WeeklyStats, WeeklyStatsWithMeta, WeekDay } from '../types/game';

interface StorageOptions {
  prefix?: string;
  version?: number;
}

export function useLocalStorage<T>(key: string, defaultValue: T, options: StorageOptions = {}) {
  const { prefix = 'guess_colleague_', version = 1 } = options;
  const fullKey = `${prefix}${key}_v${version}`;
  
  const load = (): T => {
    try {
      const stored = localStorage.getItem(fullKey);
      if (stored) {
        return JSON.parse(stored);
      }
    } catch (error) {
      console.error(`Error loading ${fullKey} from localStorage:`, error);
    }
    return defaultValue;
  };
  
  const save = (value: T) => {
    try {
      localStorage.setItem(fullKey, JSON.stringify(value));
    } catch (error) {
      console.error(`Error saving ${fullKey} to localStorage:`, error);
    }
  };
  
  const clear = () => {
    try {
      localStorage.removeItem(fullKey);
    } catch (error) {
      console.error(`Error clearing ${fullKey}:`, error);
    }
  };
  
  const data = ref<T>(load()) as Ref<T>;
  
  watch(data, (newValue) => {
    save(newValue);
  }, { deep: true });
  
  return {
    data,
    save: () => save(data.value),
    load: () => load(),
    clear
  };
}

// Специализированные хранилища
export function useGameStatsStorage(userId: number) {
  const key = `game_stats_${userId}`;
  const defaultValue = {
    score: 0,
    correctCount: 0,
    wrongCount: 0,
    currentStreak: 0,
    bestStreak: 0,
    lastUpdated: null as string | null
  };
  
  return useLocalStorage(key, defaultValue);
}

export function usePlayersStorage() {
  interface Player {
    userId: number;
    fullName: string;
    totalScore: number;
    accuracy: number;
    rank?: number;
  }
  
  return useLocalStorage<Player[]>('all_players', []);
}

export function useDailyStatsStorage() {
  const getTodayKey = () => {
    const today = new Date().toISOString().split('T')[0];
    return `daily_stats_${today}`;
  };
  
  interface DailyStats {
    activeUsers: number;
    totalGames: number;
    [key: string]: any;
  }
  
  const getTodayStats = (): DailyStats => {
    const key = getTodayKey();
    const stored = localStorage.getItem(key);
    if (stored) {
      try {
        return JSON.parse(stored);
      } catch (e) {
        return { activeUsers: 0, totalGames: 0 };
      }
    }
    return { activeUsers: 0, totalGames: 0 };
  };
  
  const saveTodayStats = (stats: DailyStats) => {
    const key = getTodayKey();
    localStorage.setItem(key, JSON.stringify(stats));
  };
  
  const recordActivity = (userId: number) => {
    const stats = getTodayStats();
    const userKey = `user_${userId}`;
    
    if (!stats[userKey]) {
      stats[userKey] = true;
      stats.activeUsers++;
    }
    
    stats.totalGames++;
    saveTodayStats(stats);
    return stats;
  };
  
  return {
    getTodayStats,
    saveTodayStats,
    recordActivity,
    getActiveToday: () => getTodayStats().activeUsers
  };
}

// Исправленный useWeeklyStatsStorage
export function useWeeklyStatsStorage() {
  // Используем дату начала недели как ключ
  const getWeekKey = (date: Date = new Date()): string => {
    const monday = getWeekStart(date);
    const year = monday.getFullYear();
    const month = String(monday.getMonth() + 1).padStart(2, '0');
    const day = String(monday.getDate()).padStart(2, '0');
    return `weekly_stats_${year}_${month}_${day}`;
  };
  
  const getWeekStart = (date: Date = new Date()): Date => {
    const result = new Date(date);
    const day = result.getDay();
    const diff = day === 0 ? 6 : day - 1;
    result.setDate(result.getDate() - diff);
    result.setHours(0, 0, 0, 0);
    return result;
  };
  
  const getWeekDay = (date: Date = new Date()): WeekDay => {
    const days: WeekDay[] = ['Вс', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб'];
    return days[date.getDay()] as WeekDay;
  };
  
  const defaultWeekStats: WeeklyStats = {
    'Пн': 0, 'Вт': 0, 'Ср': 0, 'Чт': 0, 'Пт': 0, 'Сб': 0, 'Вс': 0
  };
  
  const getCurrentWeekStats = (): WeeklyStatsWithMeta => {
    const key = getWeekKey();
    const stored = localStorage.getItem(key);
    
    console.log('Loading weekly stats from key:', key);
    
    if (stored) {
      try {
        const parsed = JSON.parse(stored);
        console.log('Loaded weekly stats:', parsed);
        return {
          Пн: parsed.Пн ?? 0,
          Вт: parsed.Вт ?? 0,
          Ср: parsed.Ср ?? 0,
          Чт: parsed.Чт ?? 0,
          Пт: parsed.Пт ?? 0,
          Сб: parsed.Сб ?? 0,
          Вс: parsed.Вс ?? 0,
          weekKey: key,
          totalScore: parsed.totalScore ?? 0
        };
      } catch (e) {
        console.error('Failed to parse weekly stats', e);
      }
    }
    
    console.log('No weekly stats found for key:', key);
    return {
      ...defaultWeekStats,
      weekKey: key,
      totalScore: 0
    };
  };
  
  const updateTodayScore = (score: number): WeeklyStatsWithMeta => {
    const stats = getCurrentWeekStats();
    const today = getWeekDay();
    
    console.log('Updating weekly stats - Today:', today, 'Score:', score);
    console.log('Current week key:', stats.weekKey);
    console.log('Before update:', stats);
    
    // Обновляем соответствующий день
    switch (today) {
      case 'Пн': stats.Пн = score; break;
      case 'Вт': stats.Вт = score; break;
      case 'Ср': stats.Ср = score; break;
      case 'Чт': stats.Чт = score; break;
      case 'Пт': stats.Пт = score; break;
      case 'Сб': stats.Сб = score; break;
      case 'Вс': stats.Вс = score; break;
    }
    
    // Обновляем общий счет
    const days: WeekDay[] = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
    stats.totalScore = days.reduce((sum, day) => sum + (stats[day] || 0), 0);
    
    console.log('After update:', stats);
    
    // Сохраняем
    localStorage.setItem(stats.weekKey, JSON.stringify(stats));
    console.log('Saved to key:', stats.weekKey);
    
    return stats;
  };
  
  const getWeeklyStatsOnly = (): WeeklyStats => {
    const { weekKey, totalScore, ...stats } = getCurrentWeekStats();
    console.log('Returning weekly stats only:', stats);
    return stats;
  };
  
  // Для отладки
  const debugAllWeeklyStats = () => {
    console.log('=== ALL WEEKLY STATS IN STORAGE ===');
    let found = false;
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.startsWith('weekly_stats_')) {
        console.log(`${key}:`, localStorage.getItem(key));
        found = true;
      }
    }
    if (!found) {
      console.log('No weekly stats found in localStorage');
    }
  };
  
  return {
    getCurrentWeekStats,
    updateTodayScore,
    getWeekDay,
    getWeekStart,
    getWeeklyStatsOnly,
    debugAllWeeklyStats
  };
}