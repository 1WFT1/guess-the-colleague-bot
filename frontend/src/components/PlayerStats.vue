<template>
  <div class="stats-modal" @click.self="$emit('close')">
    <div class="stats-content">
      <div class="stats-header">
        <h2>Моя статистика</h2>
        <button @click="$emit('close')" class="close-btn">✕</button>
      </div>
      
      <div class="stats-body">
        <div class="stats-summary">
          <div class="stat-card">
            <div class="stat-value">{{ gameStore.score }}</div>
            <div class="stat-label">Всего очков</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ gameStore.totalQuestions }}</div>
            <div class="stat-label">Сыграно вопросов</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ gameStore.correctCount }}</div>
            <div class="stat-label">Правильных</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ gameStore.wrongCount }}</div>
            <div class="stat-label">Неправильных</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ gameStore.accuracy }}%</div>
            <div class="stat-label">Точность</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ gameStore.bestStreak }}</div>
            <div class="stat-label">Лучшая серия</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ gameStore.currentStreak }}</div>
            <div class="stat-label">Текущая серия</div>
          </div>
        </div>
        
        <div class="weekly-chart">
          <h3>НЕДЕЛЬНАЯ ДИНАМИКА</h3>
          <div class="chart-bars">
            <div 
              v-for="(score, day) in weeklyStats" 
              :key="day" 
              class="chart-bar-container"
            >
              <div class="chart-bar-wrapper">
                <div 
                  class="chart-bar" 
                  :style="{ 
                    height: `${getBarHeight(score)}%`,
                    backgroundColor: '#4f4ff4'
                  }"
                >
                  <span class="bar-value">{{ score }}</span>
                </div>
              </div>
              <div class="bar-label">{{ day }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="stats-footer">
        <button @click="playGame" class="action-btn">🎮 Играть</button>
        <button @click="showLeaderboard" class="action-btn secondary">🏆 Лидерборд</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGameStore } from '../stores/game';

const gameStore = useGameStore();

const emit = defineEmits<{
  close: [];
  play: [];
  'show-leaderboard': [];
}>();

// Реальная недельная статистика из localStorage
const weeklyStats = computed(() => {
  const saved = localStorage.getItem('weeklyStats');
  if (saved) {
    try {
      return JSON.parse(saved);
    } catch (e) {
      console.error('Failed to parse weekly stats', e);
    }
  }
  
  const today = new Date().getDay();
  const days = ['Вс', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб'];
  const stats: Record<string, number> = {};
  days.forEach(day => {
    stats[day] = 0;
  });
  const todayName = days[today];
  stats[todayName] = gameStore.score;
  
  return stats;
});

const maxScore = computed(() => {
  const scores = Object.values(weeklyStats.value);
  return Math.max(...scores, 1);
});

const getBarHeight = (score: number) => {
  if (score === 0) return 10;
  return (score / maxScore.value) * 100;
};

const getBarColor = (score: number) => {
  if (score === 0) return '#3a3a3a';
  if (score === maxScore.value) return '#4f4ff4';
  const intensity = score / maxScore.value;
  return `rgba(79, 79, 244, ${0.3 + intensity * 0.7})`;
};

// Исправленные функции - используем emit вместо router
const playGame = () => {
  emit('play');
};

const showLeaderboard = () => {
  emit('show-leaderboard');
};
</script>

<style scoped>
.stats-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  transition: background 0.3s ease;
  animation: fadeInBg 0.3s ease forwards;
}

.stats-content {
  background: #1a1a1a;
  border-radius: 30px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  opacity: 0;
  transform: translateY(30px) scale(0.95);
  animation: slideInContent 0.4s cubic-bezier(0.34, 1.2, 0.64, 1) forwards;
  border: 1px solid #2a2a2a;
}

@keyframes fadeInBg {
  from {
    background: rgba(0, 0, 0, 0);
  }
  to {
    background: rgba(0, 0, 0, 0.85);
  }
}

@keyframes slideInContent {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
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

.stats-header h2 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: white;
  transition: transform 0.3s;
}

.close-btn:hover {
  transform: rotate(90deg);
}

.stats-body {
  padding: 20px;
}

.stats-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 30px;
}

.stat-card {
  background: #2a2a2a;
  padding: 15px;
  border-radius: 15px;
  text-align: center;
  transition: transform 0.3s;
  border: 1px solid #3a3a3a;
}

.stat-card:hover {
  transform: translateY(-2px);
  border-color: #4f4ff4;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #4f4ff4;
}

.stat-label {
  font-size: 12px;
  color: #888;
  margin-top: 5px;
}

.weekly-chart {
  margin-top: 20px;
}

.weekly-chart h3 {
  text-align: center;
  margin-bottom: 20px;
  color: white;
  font-size: 16px;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 250px;
  gap: 10px;
  padding: 10px 0;
}

.chart-bar-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.chart-bar-wrapper {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.chart-bar {
  width: 100%;
  max-width: 50px;
  min-width: 30px;
  background: linear-gradient(135deg, #4f4ff4 0%, #6c6cff 100%);
  border-radius: 8px 8px 0 0;
  transition: all 0.5s ease;
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 5px;
  cursor: pointer;
}

.chart-bar:hover {
  transform: scaleX(1.05);
  filter: brightness(1.1);
}

.bar-value {
  color: white;
  font-size: 11px;
  font-weight: bold;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
}

.bar-label {
  font-size: 12px;
  color: #888;
  font-weight: 500;
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
  color: white;
  border-color: #4f4ff4;
}
</style>