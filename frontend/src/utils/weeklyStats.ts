// utils/weeklyStats.ts - исправленная версия

export type WeekDay = 'Пн' | 'Вт' | 'Ср' | 'Чт' | 'Пт' | 'Сб' | 'Вс';

// Получить дату понедельника текущей недели
export const getWeekStart = (date: Date = new Date()): Date => {
  const result = new Date(date);
  const day = result.getDay();
  // В JS воскресенье = 0, понедельник = 1
  const diff = day === 0 ? 6 : day - 1;
  result.setDate(result.getDate() - diff);
  result.setHours(0, 0, 0, 0);
  return result;
};

// Получить уникальный ключ для недели (единый формат!)
export const getWeekKey = (date: Date = new Date()): string => {
  const weekStart = getWeekStart(date);
  const year = weekStart.getFullYear();
  const month = String(weekStart.getMonth() + 1).padStart(2, '0');
  const day = String(weekStart.getDate()).padStart(2, '0');
  return `weekly_stats_${year}_${month}_${day}`;
};

// Получить название дня недели (исправлено - используем as)
export const getWeekDay = (date: Date = new Date()): WeekDay => {
  const days: WeekDay[] = ['Вс', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб'];
  const dayIndex = date.getDay();
  // dayIndex всегда от 0 до 6, поэтому безопасно используем as
  return days[dayIndex] as WeekDay;
};

// Сохранить статистику за сегодня
export const saveTodayStats = (score: number): void => {
  const weekKey = getWeekKey();
  const today = getWeekDay();
  
  // Загружаем существующую статистику
  let stats: any = {};
  const existing = localStorage.getItem(weekKey);
  if (existing) {
    stats = JSON.parse(existing);
  }
  
  // Обновляем сегодняшний день
  stats[today] = score;
  
  // Обновляем общий счет
  const days: WeekDay[] = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
  stats.totalScore = days.reduce((sum, day) => sum + (stats[day] || 0), 0);
  
  // Сохраняем
  localStorage.setItem(weekKey, JSON.stringify(stats));
  console.log(`[WeeklyStats] Saved - Key: ${weekKey}, Day: ${today}, Score: ${score}`);
  console.log(`[WeeklyStats] Full data:`, stats);
};

// Загрузить статистику за текущую неделю
export const loadCurrentWeekStats = (): Record<WeekDay, number> => {
  const weekKey = getWeekKey();
  const existing = localStorage.getItem(weekKey);
  
  const defaultStats: Record<WeekDay, number> = {
    'Пн': 0, 'Вт': 0, 'Ср': 0, 'Чт': 0, 'Пт': 0, 'Сб': 0, 'Вс': 0
  };
  
  if (existing) {
    try {
      const data = JSON.parse(existing);
      console.log(`[WeeklyStats] Loaded from ${weekKey}:`, data);
      return {
        Пн: data.Пн || 0,
        Вт: data.Вт || 0,
        Ср: data.Ср || 0,
        Чт: data.Чт || 0,
        Пт: data.Пт || 0,
        Сб: data.Сб || 0,
        Вс: data.Вс || 0
      };
    } catch (e) {
      console.error('[WeeklyStats] Failed to parse:', e);
    }
  }
  
  console.log(`[WeeklyStats] No data for ${weekKey}, using defaults`);
  return defaultStats;
};

// Очистить все старые статистики (оставить только текущую неделю)
export const cleanupOldWeeklyStats = (): void => {
  const currentKey = getWeekKey();
  let removed = 0;
  
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i);
    if (key && key.startsWith('weekly_stats_') && key !== currentKey) {
      localStorage.removeItem(key);
      removed++;
      console.log(`[WeeklyStats] Removed old: ${key}`);
    }
  }
  
  if (removed > 0) {
    console.log(`[WeeklyStats] Cleaned up ${removed} old weekly stats`);
  }
};

// Для отладки - показать все недельные статистики
export const debugAllWeeklyStats = (): void => {
  console.log('=== ALL WEEKLY STATS ===');
  let found = false;
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i);
    if (key && key.startsWith('weekly_stats_')) {
      const data = localStorage.getItem(key);
      console.log(`${key}:`, data);
      found = true;
    }
  }
  if (!found) {
    console.log('No weekly stats found');
  }
};

// Получить текущую недельную статистику в удобном формате
export const getCurrentWeekStatsFormatted = () => {
  const stats = loadCurrentWeekStats();
  const days = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
  return days.map(day => ({
    day,
    score: stats[day as WeekDay]
  }));
};