<!-- components/PlayerStats.vue - исправленная версия -->
<template>
  <div class="stats-modal" @click.self="$emit('close')">
    <div class="stats-content">
      <div class="stats-header">
        <h2>📊 Моя статистика</h2>
        <button @click="$emit('close')" class="close-btn">✕</button>
      </div>
      
      <div class="stats-body">
        <!-- Основная статистика -->
        <div class="stats-summary">
          <StatCard
            v-for="stat in mainStats"
            :key="stat.label"
            :value="stat.value"
            :label="stat.label"
            :color="stat.color"
            :suffix="stat.suffix"
          />
        </div>
        
        <!-- Дополнительная статистика -->
        <div class="stats-details">
          <div class="details-card">
            <h4>📈 Детальная статистика</h4>
            <div class="details-grid">
              <div class="detail-item">
                <span class="detail-label">Всего вопросов:</span>
                <span class="detail-value">{{ gameStore.totalQuestions }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Правильных:</span>
                <span class="detail-value correct">{{ gameStore.correctCount }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Неправильных:</span>
                <span class="detail-value wrong">{{ gameStore.wrongCount }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Соотношение:</span>
                <span class="detail-value">{{ getRatio }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Текущая серия:</span>
                <span class="detail-value streak">{{ gameStore.currentStreak }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Рекордная серия:</span>
                <span class="detail-value streak-best">{{ gameStore.bestStreak }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Недельная динамика -->
        <div class="weekly-chart">
          <h3>НЕДЕЛЬНАЯ ДИНАМИКА</h3>
          <WeeklyChart :weekly-stats="weeklyStatsAsRecord" />
        </div>
        
        <!-- Достижения -->
        <div v-if="achievements.length > 0" class="achievements">
          <h3>🏅 Достижения</h3>
          <div class="achievements-grid">
            <div 
              v-for="achievement in achievements" 
              :key="achievement.id"
              class="achievement-card"
              :class="{ unlocked: achievement.unlocked }"
            >
              <div class="achievement-icon">{{ achievement.icon }}</div>
              <div class="achievement-name">{{ achievement.name }}</div>
              <div class="achievement-desc">{{ achievement.description }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="stats-footer">
        <button @click="$emit('play')" class="action-btn">🎮 Играть</button>
        <button @click="$emit('show-leaderboard')" class="action-btn secondary">🏆 Лидерборд</button>
        <button v-if="canReset" @click="resetStats" class="action-btn danger">🔄 Сбросить</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { useGameStore } from '../stores/game';
import StatCard from './common/StatCard.vue';
import WeeklyChart from './common/WeeklyChart.vue';
import { useAchievements } from '../composables/useAchievements';
import type { WeeklyStats } from '../types/game';

const emit = defineEmits<{
  close: [];
  play: [];
  'show-leaderboard': [];
}>();

const gameStore = useGameStore();
const achievementsService = useAchievements();

const getRatio = computed(() => {
  const total = gameStore.totalQuestions;
  if (total === 0) return '0%';
  const ratio = (gameStore.correctCount / total) * 100;
  return `${ratio.toFixed(1)}%`;
});

const mainStats = computed(() => [
  { value: gameStore.score, label: 'Всего очков', color: '#4f4ff4', suffix: '' },
  { value: gameStore.accuracy, label: 'Точность', color: '#4caf50', suffix: '%' },
  { value: gameStore.totalQuestions, label: 'Вопросов', color: '#ff9800', suffix: '' },
  { value: gameStore.bestStreak, label: 'Лучшая серия', color: '#f44336', suffix: '' }
]);

const weeklyStats = computed((): WeeklyStats => {
  if (gameStore.getCurrentWeeklyStats && typeof gameStore.getCurrentWeeklyStats === 'function') {
    const stats = gameStore.getCurrentWeeklyStats();
    console.log('Weekly stats from store:', stats);
    return stats;
  }
  return { 'Пн': 0, 'Вт': 0, 'Ср': 0, 'Чт': 0, 'Пт': 0, 'Сб': 0, 'Вс': 0 };
});

const weeklyStatsAsRecord = computed((): Record<string, number> => {
  const result = { ...weeklyStats.value };
  console.log('Weekly stats as record:', result);
  return result;
});

const achievements = computed(() => achievementsService.getAchievements(gameStore));

const canReset = computed(() => {
  return gameStore.score > 0 || gameStore.totalQuestions > 0;
});

const resetStats = () => {
  if (confirm('Вы уверены, что хотите сбросить всю статистику? Это действие нельзя отменить!')) {
    gameStore.resetStats();
    achievementsService.resetAchievements();
  }
};

onMounted(() => {
  achievementsService.checkAchievements(gameStore);
  
  // Debug: проверяем localStorage
  console.log('=== PLAYER STATS DEBUG ===');
  console.log('Current score:', gameStore.score);
  
  // Проверяем все weekly stats в localStorage
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i);
    if (key && key.startsWith('weekly_stats_')) {
      console.log(`${key}:`, localStorage.getItem(key));
    }
  }
});
</script>

<style scoped>
.stats-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.stats-content {
  background: #1a1a1a;
  border-radius: 30px;
  max-width: 700px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  animation: slideIn 0.4s ease;
  border: 1px solid #2a2a2a;
}

@keyframes slideIn {
  from { transform: translateY(30px) scale(0.95); opacity: 0; }
  to { transform: translateY(0) scale(1); opacity: 1; }
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #2a2a2a;
  background: linear-gradient(135deg, #4f4ff4 0%, #6c6cff 100%);
  color: white;
  border-radius: 30px 30px 0 0;
}

.stats-header h2 { margin: 0; }

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: white;
  transition: transform 0.3s;
}

.close-btn:hover { transform: rotate(90deg); }

.stats-body { padding: 20px; }

.stats-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  margin-bottom: 25px;
}

.stats-details { margin-bottom: 25px; }

.details-card {
  background: #2a2a2a;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #3a3a3a;
}

.details-card h4 {
  color: white;
  margin: 0 0 15px 0;
  font-size: 16px;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #1a1a1a;
  border-radius: 10px;
}

.detail-label {
  font-size: 13px;
  color: #888;
}

.detail-value {
  font-size: 16px;
  font-weight: bold;
  color: #4f4ff4;
}

.detail-value.correct { color: #4caf50; }
.detail-value.wrong { color: #f44336; }
.detail-value.streak { color: #ff9800; }
.detail-value.streak-best { color: #ffc107; }

.weekly-chart {
  margin-top: 25px;
  margin-bottom: 25px;
}

.weekly-chart h3 {
  text-align: center;
  margin-bottom: 20px;
  color: white;
  font-size: 16px;
}

.achievements { margin-top: 25px; }

.achievements h3 {
  color: white;
  font-size: 16px;
  margin-bottom: 15px;
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
}

.achievement-card {
  background: #2a2a2a;
  border-radius: 12px;
  padding: 12px;
  text-align: center;
  opacity: 0.5;
  transition: all 0.3s;
  border: 1px solid #3a3a3a;
}

.achievement-card.unlocked {
  opacity: 1;
  background: linear-gradient(135deg, #2a2a2a 0%, #1a2a3a 100%);
  border-color: #ffd700;
}

.achievement-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.achievement-name {
  font-size: 14px;
  font-weight: bold;
  color: white;
  margin-bottom: 4px;
}

.achievement-desc {
  font-size: 10px;
  color: #888;
}

.stats-footer {
  padding: 20px;
  display: flex;
  gap: 15px;
  border-top: 1px solid #2a2a2a;
}

.action-btn {
  flex: 1;
  padding: 12px;
  background: #4f4ff4;
  color: white;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  transition: all 0.3s;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(79, 79, 244, 0.3);
}

.action-btn.secondary {
  background: #2a2a2a;
  color: #4f4ff4;
  border: 1px solid #3a3a3a;
}

.action-btn.secondary:hover {
  background: #4f4ff4;
  color: white;
}

.action-btn.danger {
  background: #dc2626;
  color: white;
}

.action-btn.danger:hover {
  background: #ef4444;
}

@media (max-width: 600px) {
  .stats-summary {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .details-grid {
    grid-template-columns: 1fr;
  }
  
  .achievements-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>