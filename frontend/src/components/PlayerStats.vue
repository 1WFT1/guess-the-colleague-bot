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
            <div v-for="(score, day) in weeklyStats" :key="day" class="chart-bar-container">
              <div class="chart-bar" :style="{ height: `${getBarHeight(score)}%` }">
                <span class="bar-value">{{ score }}</span>
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
import { useRouter } from 'vue-router';

const gameStore = useGameStore();
const router = useRouter();

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
  
  // Если нет сохраненной статистики, показываем текущие данные
  return {
    'Пн': gameStore.score || 0,
    'Вт': 0,
    'Ср': 0,
    'Чт': 0,
    'Пт': 0,
    'Сб': 0,
    'Вс': 0
  };
});

const getBarHeight = (score: number) => {
  const maxScore = Math.max(...Object.values(weeklyStats.value), 100);
  return (score / maxScore) * 100;
};

const playGame = () => {
  router.push('/game');
};

const showLeaderboard = () => {
  router.push('/leaderboard');
};

defineEmits<{
  close: [];
}>();
</script>

<style scoped>
.stats-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.stats-content {
  background: white;
  border-radius: 30px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  background: #f8f9fa;
  padding: 15px;
  border-radius: 15px;
  text-align: center;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #667eea;
}

.stat-label {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

.weekly-chart h3 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
  font-size: 16px;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 200px;
  gap: 10px;
}

.chart-bar-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.chart-bar {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 5px 5px 0 0;
  transition: height 0.3s;
  position: relative;
  min-height: 30px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 5px;
}

.bar-value {
  color: white;
  font-size: 11px;
  font-weight: bold;
}

.bar-label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.stats-footer {
  padding: 20px;
  display: flex;
  gap: 15px;
  border-top: 1px solid #e0e0e0;
}

.action-btn {
  flex: 1;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  box-shadow: 0 5px 15px rgba(102,126,234,0.3);
}

.action-btn.secondary {
  background: #f0f0f0;
  color: #667eea;
}
</style>