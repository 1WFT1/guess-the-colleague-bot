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
          <div class="stat-card"><div class="stat-value">{{ gameStore.score }}</div><div class="stat-label">Всего очков</div></div>
          <div class="stat-card"><div class="stat-value">{{ gameStore.accuracy }}%</div><div class="stat-label">Точность</div></div>
          <div class="stat-card"><div class="stat-value">{{ gameStore.totalQuestions }}</div><div class="stat-label">Вопросов</div></div>
          <div class="stat-card"><div class="stat-value">{{ gameStore.bestStreak }}</div><div class="stat-label">Лучшая серия</div></div>
        </div>
        
        <!-- Детальная статистика -->
        <div class="stats-details">
          <div class="details-card">
            <h4>📈 Детальная статистика</h4>
            <div class="details-grid">
              <div class="detail-item"><span class="detail-label">Всего вопросов:</span><span class="detail-value">{{ gameStore.totalQuestions }}</span></div>
              <div class="detail-item"><span class="detail-label">Правильных:</span><span class="detail-value correct">{{ gameStore.correctCount }}</span></div>
              <div class="detail-item"><span class="detail-label">Неправильных:</span><span class="detail-value wrong">{{ gameStore.wrongCount }}</span></div>
              <div class="detail-item"><span class="detail-label">Соотношение:</span><span class="detail-value">{{ getRatio }}</span></div>
              <div class="detail-item"><span class="detail-label">Текущая серия:</span><span class="detail-value streak">{{ gameStore.currentStreak }}</span></div>
              <div class="detail-item"><span class="detail-label">Рекордная серия:</span><span class="detail-value streak-best">{{ gameStore.bestStreak }}</span></div>
            </div>
          </div>
        </div>
        
        <!-- Достижения -->
        <div class="achievements-section">
          <h4>🏅 Достижения ({{ achievements.getUnlockedCount() }}/{{ achievements.getTotalCount() }})</h4>
          <div class="achievements-grid">
            <div v-for="ach in achievements.achievements.value" :key="ach.id" 
                 class="achievement-card" :class="{ unlocked: ach.unlocked }">
              <div class="achievement-icon">{{ ach.icon }}</div>
              <div class="achievement-name">{{ ach.name }}</div>
              <div class="achievement-desc">{{ ach.description }}</div>
            </div>
          </div>
          <div class="progress-bar-container">
            <div class="progress-label">Прогресс: {{ achievements.getProgress() }}%</div>
            <div class="progress-bar"><div class="progress-fill" :style="{ width: achievements.getProgress() + '%' }"></div></div>
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
import { computed, onMounted, watch } from 'vue';
import { GameStore } from '../../stores/game';
import { useAchievements } from '../../composables/useAchievements';

const emit = defineEmits<{ close: []; play: []; 'show-leaderboard': [] }>();
const gameStore = GameStore();
const achievements = useAchievements();

const getRatio = computed(() => {
  const total = gameStore.totalQuestions;
  if (total === 0) return '0%';
  return `${((gameStore.correctCount / total) * 100).toFixed(1)}%`;
});

const canReset = computed(() => {
  return gameStore.score > 0 || gameStore.totalQuestions > 0;
});

const resetStats = async () => {
  if (confirm('Вы уверены, что хотите сбросить ВСЮ статистику?\n\nБудут сброшены:\n- Все очки\n- Правильные/неправильные ответы\n- Серии\n\nЭто действие нельзя отменить!')) {
    await gameStore.resetStats();
    achievements.resetAchievements();
  }
};

// Проверяем достижения при изменении статистики
watch([() => gameStore.score, () => gameStore.correctCount, () => gameStore.bestStreak, () => gameStore.totalQuestions], () => {
  achievements.checkAchievements(gameStore);
}, { immediate: true });

onMounted(() => {
  achievements.checkAchievements(gameStore);
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
  max-width: 600px;
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
}

.achievements-section {
  margin-top: 25px;
  background: #2a2a2a;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #3a3a3a;
}

.achievements-section h4 {
  color: white;
  margin: 0 0 15px 0;
  font-size: 16px;
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.achievement-card {
  background: #1a1a1a;
  border-radius: 12px;
  padding: 12px;
  text-align: center;
  opacity: 0.4;
  transition: all 0.3s;
  border: 1px solid #3a3a3a;
}

.achievement-card.unlocked {
  opacity: 1;
  background: linear-gradient(135deg, #1a2a3a 0%, #2a2a2a 100%);
  border-color: #ffd700;
}

.achievement-icon { font-size: 32px; margin-bottom: 8px; }
.achievement-name { font-size: 14px; font-weight: bold; color: white; margin-bottom: 4px; }
.achievement-desc { font-size: 10px; color: #888; }

.progress-bar-container { margin-top: 15px; }
.progress-label { font-size: 12px; color: #888; margin-bottom: 5px; }
.progress-bar { height: 6px; background: #3a3a3a; border-radius: 10px; overflow: hidden; }
.progress-fill { height: 100%; background: #ffd700; border-radius: 10px; transition: width 0.3s; }
</style>