<template>
  <div class="game-board">
    <!-- Переключатель режимов игры -->
    <div class="game-mode-switch">
      <button 
        :class="['mode-btn', { active: currentGameMode === 'name' }]"
        @click="setGameMode('name')"
      >
        👤 Угадать сотрудника
      </button>
      <button 
        :class="['mode-btn', { active: currentGameMode === 'department' }]"
        @click="setGameMode('department')"
      >
        🏢 Угадать отдел
      </button>
    </div>

    <!-- Статистика -->
    <div class="stats-header">
      <div class="stat-card score-card">
        <div class="stat-icon">
          <span class="stat-icon-emoji">⭐</span>
        </div>
        <div class="stat-content">
          <div class="stat-label">ОЧКИ</div>
          <div class="stat-value">{{ gameStore.score }}</div>
        </div>
      </div>

      <div v-if="gameStore.currentStreak > 0 || gameStore.bestStreak > 0" class="stat-card streak-card">
        <div class="stat-icon">🔥</div>
        <div class="stat-content">
          <div class="stat-label">СЕРИЯ</div>
          <div class="stat-value">{{ gameStore.currentStreak }}</div>
          <div class="stat-record-full">
            <span class="record-label">РЕКОРД</span>
            <span class="record-value">{{ gameStore.bestStreak }}</span>
          </div>
        </div>
      </div>

      <div class="stat-card stats-card">
        <div class="stat-icons">
          <div class="stat-badge correct">
            <span class="badge-icon">✓</span>
            <span class="badge-count">{{ gameStore.correctCount }}</span>
          </div>
          <div class="stat-badge wrong">
            <span class="badge-icon">✗</span>
            <span class="badge-count">{{ gameStore.wrongCount }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Сообщение об ошибке -->
    <div v-if="gameStore.error" class="error-message">
      ⚠️ {{ gameStore.error }}
      <button @click="retryGame" class="retry-btn">Повторить</button>
    </div>

    <!-- Вопрос -->
    <div v-if="gameStore.currentQuestion && !gameStore.isLoading" class="question-area">
      <div class="photo-container">
        <div class="photo-wrapper">
          <img 
            :src="currentPhotoUrl" 
            :alt="'Фото сотрудника'"
            @error="handleImageError"
            class="employee-photo"
          />
          <div class="photo-glow"></div>
        </div>
      </div>

      <h3 class="question-text">
        {{ currentGameMode === 'department' ? 'В каком отделе работает?' : 'Кто это?' }}
      </h3>

      <div class="options-grid">
        <button
          v-for="(option, index) in gameStore.currentQuestion.options"
          :key="index"
          @click="handleAnswer(index)"
          :disabled="isAnswerDisabled || isAnswerSubmitted"
          class="option-btn"
          :class="{ 'correct-answer': showCorrectAnswer && option === correctAnswerName }"
        >
          <span class="option-letter">{{ getOptionLetter(index) }}</span>
          <span class="option-text">{{ option }}</span>
        </button>
      </div>

      <!-- Feedback оверлей -->
      <div v-if="gameStore.feedback" class="feedback-overlay" @click="handleFeedbackClick">
        <div class="feedback-card" :class="feedbackCardClass">
          <div class="feedback-title">{{ feedbackTitle }}</div>
          <div class="feedback-points">{{ feedbackPoints }}</div>
          <div v-if="!gameStore.feedback.correct" class="feedback-details">
            Правильный ответ: <strong>{{ gameStore.feedback.correctAnswer }}</strong>
          </div>
          <div v-if="gameStore.currentStreak > 0" class="feedback-streak">
            🔥 Серия: {{ gameStore.currentStreak }} {{ getStreakText }}
          </div>
          <div class="loading-next">Загрузка следующего вопроса...</div>
        </div>
      </div>
    </div>

    <!-- Загрузка -->
    <div v-else-if="gameStore.isLoading && !gameStore.currentQuestion" class="loading-area">
      <div class="loader"></div>
      <p>Загрузка первого вопроса...</p>
    </div>
    
    <Mascot 
      :mood="mascotMood" 
      :message="mascotMessage"
    />
    
    <button @click="backToMenu" class="menu-back-btn">
      ← Вернуться в меню
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, watch } from 'vue';
import { GameStore } from '../../stores/game';
import Mascot from '../common/Mascot.vue';
import { useAchievements } from '../../composables/useAchievements';

const props = defineProps<{
  userId: number;
  chatId?: number;
  gameMode?: 'name' | 'department';
}>();

const emit = defineEmits<{
  'back-to-menu': [];
  'update:gameMode': [mode: 'name' | 'department'];
}>();

const achievements = useAchievements();
const gameStore = GameStore();
const showCorrectAnswer = ref(false);
const correctAnswerName = ref('');
const currentGameMode = ref(props.gameMode || 'name');
const isAnswerSubmitted = ref(false);  // <-- ДОБАВЬТЕ ЭТУ СТРОКУ
let feedbackTimeout: ReturnType<typeof setTimeout> | null = null;

// Сбрасываем флаг при загрузке нового вопроса
watch(() => gameStore.currentQuestion, () => {
  isAnswerSubmitted.value = false;
});

const setGameMode = async (mode: 'name' | 'department') => {
  if (currentGameMode.value === mode) return;
  currentGameMode.value = mode;
  
  // Обновляем режим на бэкенде
  try {
    await gameStore.updateGameMode(mode);
    console.log('Game mode updated to:', mode);
  } catch (err) {
    console.error('Failed to update game mode:', err);
  }
  
  emit('update:gameMode', mode);
};
// Computed
const currentPhotoUrl = computed(() => {
  return gameStore.currentQuestion?.photoUrl || 'https://via.placeholder.com/200x200/667eea/ffffff?text=👤';
});

const isAnswerDisabled = computed(() => {
  return gameStore.isLoading || !!gameStore.feedback;
});

const feedbackCardClass = computed(() => ({
  correct: gameStore.feedback?.correct === true,
  wrong: gameStore.feedback?.correct === false
}));

const feedbackTitle = computed(() => {
  return gameStore.feedback?.correct ? 'ВЕРНО!' : 'ОШИБКА!';
});

const feedbackPoints = computed(() => {
  const delta = gameStore.feedback?.pointsDelta || 0;
  return `${delta > 0 ? '+' : ''}${delta} баллов`;
});

const getStreakText = computed(() => {
  const streak = gameStore.currentStreak;
  if (streak % 10 === 1 && streak % 100 !== 11) return 'правильный ответ подряд!';
  if ([2, 3, 4].includes(streak % 10) && ![12, 13, 14].includes(streak % 100)) return 'правильных ответа подряд!';
  return 'правильных ответов подряд!';
});

const mascotMood = computed(() => {
  if (gameStore.feedback) {
    return gameStore.feedback.correct ? 'happy' : 'sad';
  }
  if (gameStore.isLoading) return 'thinking';
  return 'neutral';
});

const mascotMessage = computed(() => {
  if (gameStore.feedback) {
    return gameStore.feedback.correct 
      ? 'Отлично! Так держать! Продолжай в том же духе!' 
      : `Ничего страшного! Правильный ответ: ${gameStore.feedback.correctAnswer}`;
  }
  if (gameStore.isLoading && !gameStore.currentQuestion) return 'Загружаем первый вопрос...';
  if (gameStore.isLoading) return 'Думаю над следующим вопросом...';
  if (gameStore.score === 0 && !gameStore.currentQuestion) return 'Привет! Давай проверим, как хорошо ты знаешь коллег!';
  if (gameStore.currentStreak >= 5) return 'Вау! У тебя невероятная серия! Так держать!';
  if (gameStore.currentStreak >= 3) return 'Отличная серия! Продолжай в том же духе!';
  
  if (currentGameMode.value === 'department') {
    return 'В каком отделе работает этот сотрудник? Выбери правильный отдел!';
  }
  return 'Кто это на фото? Выбери правильный ответ!';
});

// Methods
const getOptionLetter = (index: number): string => {
  return String.fromCharCode(65 + index);
};

const handleAnswer = async (index: number) => {
  // Блокируем повторные ответы
  if (isAnswerDisabled.value || !gameStore.currentQuestion || isAnswerSubmitted.value) {
    console.log('Answer already submitted or disabled');
    return;
  }
  
  isAnswerSubmitted.value = true;
  
  console.log('Submitting answer for question:', gameStore.currentQuestion.questionId);
  
  await gameStore.submitAnswer(index);

  setTimeout(() => {
    achievements.checkAchievements(gameStore);
  }, 100);

  if (!gameStore.feedback?.correct) {
    showCorrectAnswer.value = true;
    correctAnswerName.value = gameStore.feedback?.correctAnswer || '';
    
    feedbackTimeout = setTimeout(() => {
      showCorrectAnswer.value = false;
    }, 2000);
  }
};

const handleFeedbackClick = () => {};

const retryGame = () => {
  gameStore.resetGame();
  gameStore.initGame(props.userId, props.chatId || props.userId, currentGameMode.value);
};

const backToMenu = () => {
  if (feedbackTimeout) {
    clearTimeout(feedbackTimeout);
    feedbackTimeout = null;
  }
  emit('back-to-menu');
};

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.src = 'https://via.placeholder.com/200x200/667eea/ffffff?text=👤';
};

onUnmounted(() => {
  if (feedbackTimeout) {
    clearTimeout(feedbackTimeout);
  }
});
</script>

<style scoped>
/* Стили для переключателя режимов */
.game-mode-switch {
  display: flex;
  gap: 10px;
  padding: 16px;
  background: #1a1a1a;
  border-bottom: 1px solid #2a2a2a;
}

.mode-btn {
  flex: 1;
  padding: 10px;
  background: #2a2a2a;
  border: 1px solid #3a3a3a;
  border-radius: 12px;
  color: #888;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  font-weight: 500;
}

.mode-btn.active {
  background: #4f4ff4;
  color: white;
  border-color: #4f4ff4;
}

.mode-btn:hover:not(.active) {
  background: #3a3a3a;
  color: #e0e0e0;
}

/* Остальные стили из вашего файла */
.game-board {
  max-width: 600px;
  margin: 0 auto;
  background: #1a1a1a;
  border-radius: 30px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
  animation: slideIn 0.5s ease;
  border: 1px solid #2a2a2a;
}

@keyframes slideIn {
  from { transform: translateY(50px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.stats-header {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #1a1a1a;
  border-bottom: 1px solid #2a2a2a;
}

.stat-card {
  flex: 1;
  min-width: 0;
  background: #2a2a2a;
  border-radius: 16px;
  padding: 6px 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  border: 1px solid #3a3a3a;
}

.score-card { flex: 0.8; }
.streak-card { flex: 0.9; }
.stats-card { flex: 1; }

.stat-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 9px;
  color: #888;
  letter-spacing: 0.5px;
  margin-bottom: 2px;
  white-space: nowrap;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #4f4ff4;
  line-height: 1;
  white-space: nowrap;
}

.streak-card .stat-value {
  color: #ff9800;
}

.stat-record-full {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 4px;
  margin-top: 2px;
}

.record-label {
  font-size: 9px;
  color: #888;
  letter-spacing: 0.5px;
}

.record-value {
  font-size: 11px;
  font-weight: bold;
  color: #ff9800;
  background: rgba(255, 152, 0, 0.15);
  padding: 1px 6px;
  border-radius: 12px;
}

.stat-icons {
  display: flex;
  gap: 8px;
  justify-content: space-between;
  width: 100%;
}

.stat-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 30px;
  font-size: 13px;
  font-weight: 600;
  flex: 1;
}

.stat-badge.correct {
  background: rgba(76, 175, 80, 0.15);
  color: #4caf50;
}

.stat-badge.wrong {
  background: rgba(244, 67, 54, 0.15);
  color: #f44336;
}

.badge-icon { font-size: 12px; }
.badge-count { font-size: 14px; font-weight: bold; }

.question-area {
  padding: 30px;
  padding-bottom: 0px;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.photo-container {
  text-align: center;
  margin-bottom: 30px;
}

.photo-wrapper {
  position: relative;
  display: inline-block;
}

.employee-photo {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid white;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  position: relative;
  z-index: 1;
}

.photo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(79,79,244,0.3) 0%, rgba(79,79,244,0) 70%);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.5; }
  50% { transform: translate(-50%, -50%) scale(1.1); opacity: 0.8; }
}

.question-text {
  text-align: center;
  color: #4f4ff4;
  margin-bottom: 30px;
  font-size: 24px;
  font-weight: bold;
}

.options-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}

.option-btn {
  position: relative;
  padding: 15px;
  background: #2a2a2a;
  border: 1px solid #3a3a3a;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #ffffff;
}

.option-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  background: #4f4ff4;
  border-color: #4f4ff4;
  color: white;
}

.option-btn.correct-answer {
  background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
  border-color: #4caf50;
  color: white;
  animation: correctFlash 0.5s ease;
}

@keyframes correctFlash {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.option-letter {
  width: 30px;
  height: 30px;
  background: #4f4ff4;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.option-btn:hover:not(:disabled) .option-letter {
  background: white;
  color: #4f4ff4;
}

.option-text {
  flex: 1;
  font-weight: 500;
  text-align: left;
}

.feedback-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: transparent !important;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  z-index: 1000;
  padding-top: 150px;
  pointer-events: none;
}

.feedback-card {
  background: white;
  padding: 25px 35px;
  border-radius: 20px;
  text-align: center;
  max-width: 320px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  pointer-events: auto;
  padding-top: 500px;
  margin-top: 75px
}


.feedback-card.correct {
  background: linear-gradient(135deg, #2e7d32 0%, #1b5e20 100%);
  color: white;
}

.feedback-card.wrong {
  background: linear-gradient(135deg, #c62828 0%, #b71c1c 100%);
  color: white;
}

.feedback-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.feedback-points {
  font-size: 18px;
  margin-bottom: 12px;
  font-weight: bold;
}

.feedback-details {
  margin: 12px 0;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  font-size: 14px;
}

.feedback-streak {
  margin: 12px 0;
  font-size: 13px;
  padding: 5px 10px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  display: inline-block;
}

.loading-next {
  margin-top: 12px;
  font-size: 11px;
  opacity: 0.8;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.error-message {
  background: #c62828;
  color: white;
  padding: 15px;
  margin: 20px;
  border-radius: 10px;
  text-align: center;
}

.retry-btn {
  margin-left: 10px;
  padding: 5px 15px;
  background: white;
  color: #c62828;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.loading-area {
  text-align: center;
  padding: 60px 20px;
}

.loader {
  width: 50px;
  height: 50px;
  border: 3px solid #2a2a2a;
  border-top: 3px solid #4f4ff4;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.menu-back-btn {
  display: block;
  width: calc(100% - 40px);
  margin: 20px;
  padding: 12px;
  background: #2a2a2a;
  border: 1px solid #3a3a3a;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  color: #4f4ff4;
  transition: all 0.3s;
}

.menu-back-btn:hover {
  background: #4f4ff4;
  color: white;
}

@media (max-width: 500px) {
  .options-grid { grid-template-columns: 1fr; }
  .employee-photo { width: 150px; height: 150px; }
  .question-text { font-size: 20px; }
  .stats-header { gap: 8px; padding: 12px; }
  .stat-value { font-size: 16px; }
}
/* Стили остаются те же, что и в оригинале */
.game-board {
  max-width: 600px;
  margin: 0 auto;
  background: #1a1a1a;
  border-radius: 30px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
  animation: slideIn 0.5s ease;
  border: 1px solid #2a2a2a;
}

@keyframes slideIn {
  from { transform: translateY(50px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.stats-header {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #1a1a1a;
  border-bottom: 1px solid #2a2a2a;
}

.stat-card {
  flex: 1;
  min-width: 0;
  background: #2a2a2a;
  border-radius: 16px;
  padding: 6px 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  border: 1px solid #3a3a3a;
}

.score-card { flex: 0.8; }
.streak-card { flex: 0.9; }
.stats-card { flex: 1; }

.stat-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.stat-icon-img {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 9px;
  color: #888;
  letter-spacing: 0.5px;
  margin-bottom: 2px;
  white-space: nowrap;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #4f4ff4;
  line-height: 1;
  white-space: nowrap;
}

.streak-card .stat-value {
  color: #ff9800;
}

.stat-record-full {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 4px;
  margin-top: 2px;
}

.record-label {
  font-size: 9px;
  color: #888;
  letter-spacing: 0.5px;
}

.record-value {
  font-size: 11px;
  font-weight: bold;
  color: #ff9800;
  background: rgba(255, 152, 0, 0.15);
  padding: 1px 6px;
  border-radius: 12px;
}

.stat-icons {
  display: flex;
  gap: 8px;
  justify-content: space-between;
  width: 100%;
}

.stat-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 30px;
  font-size: 13px;
  font-weight: 600;
  flex: 1;
}

.stat-badge.correct {
  background: rgba(76, 175, 80, 0.15);
  color: #4caf50;
}

.stat-badge.wrong {
  background: rgba(244, 67, 54, 0.15);
  color: #f44336;
}

.badge-icon { font-size: 12px; }
.badge-count { font-size: 14px; font-weight: bold; }

.question-area {
  padding: 30px;
  padding-bottom: 0px;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.photo-container {
  text-align: center;
  margin-bottom: 30px;
}

.photo-wrapper {
  position: relative;
  display: inline-block;
}

.employee-photo {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid white;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  position: relative;
  z-index: 1;
}

.photo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(79,79,244,0.3) 0%, rgba(79,79,244,0) 70%);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.5; }
  50% { transform: translate(-50%, -50%) scale(1.1); opacity: 0.8; }
}

.question-text {
  text-align: center;
  color: #4f4ff4;
  margin-bottom: 30px;
  font-size: 24px;
  font-weight: bold;
}

.options-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}

.option-btn {
  position: relative;
  padding: 15px;
  background: #2a2a2a;
  border: 1px solid #3a3a3a;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #ffffff;
}

.option-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  background: #4f4ff4;
  border-color: #4f4ff4;
  color: white;
}

.option-btn.correct-answer {
  background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
  border-color: #4caf50;
  color: white;
  animation: correctFlash 0.5s ease;
}

@keyframes correctFlash {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.option-letter {
  width: 30px;
  height: 30px;
  background: #4f4ff4;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.option-btn:hover:not(:disabled) .option-letter {
  background: white;
  color: #4f4ff4;
}

.option-text {
  flex: 1;
  font-weight: 500;
  text-align: left;
}

.feedback-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  z-index: 1000;
  padding-top: 150px;
  animation: fadeIn 0.2s ease;
  pointer-events: auto;
}

.feedback-card {
  background: white;
  padding: 25px 35px;
  border-radius: 20px;
  text-align: center;
  max-width: 320px;
  animation: slideIn 0.3s ease;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideIn {
  from {
    transform: translateY(-30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.feedback-card.correct {
  background: linear-gradient(135deg, #2e7d32 0%, #1b5e20 100%);
  color: white;
}

.feedback-card.wrong {
  background: linear-gradient(135deg, #c62828 0%, #b71c1c 100%);
  color: white;
}
.feedback-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.feedback-points {
  font-size: 18px;
  margin-bottom: 12px;
  font-weight: bold;
}

.feedback-details {
  margin: 12px 0;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  font-size: 14px;
}

.feedback-streak {
  margin: 12px 0;
  font-size: 13px;
  padding: 5px 10px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  display: inline-block;
}

.loading-next {
  margin-top: 12px;
  font-size: 11px;
  opacity: 0.8;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.error-message {
  background: #c62828;
  color: white;
  padding: 15px;
  margin: 20px;
  border-radius: 10px;
  text-align: center;
}

.retry-btn {
  margin-left: 10px;
  padding: 5px 15px;
  background: white;
  color: #c62828;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.loading-area {
  text-align: center;
  padding: 60px 20px;
}

.loader {
  width: 50px;
  height: 50px;
  border: 3px solid #2a2a2a;
  border-top: 3px solid #4f4ff4;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.menu-back-btn {
  display: block;
  width: calc(100% - 40px);
  margin: 20px;
  padding: 12px;
  background: #2a2a2a;
  border: 1px solid #3a3a3a;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  color: #4f4ff4;
  transition: all 0.3s;
}

.menu-back-btn:hover {
  background: #4f4ff4;
  color: white;
}

@media (max-width: 500px) {
  .options-grid { grid-template-columns: 1fr; }
  .employee-photo { width: 150px; height: 150px; }
  .question-text { font-size: 20px; }
  .stats-header { gap: 8px; padding: 12px; }
  .stat-value { font-size: 16px; }
}

</style>
