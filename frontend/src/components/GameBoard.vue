<template>
  <div class="game-board">
    <div class="score-header">
      <div class="score-card">
        <div class="score-icon">🎯</div>
        <div class="score-info">
          <div class="score-label">ОЧКИ</div>
          <div class="score-value">{{ gameStore.score }}</div>
        </div>
      </div>
      <div class="streak-card" v-if="gameStore.currentStreak > 0 || gameStore.bestStreak > 0">
        <div class="streak-icon">🔥</div>
        <div class="streak-info">
          <div class="streak-label">СЕРИЯ</div>
          <div class="streak-value">{{ gameStore.currentStreak }}</div>
          <div class="best-streak">Рекорд: {{ gameStore.bestStreak }}</div>
        </div>
      </div>
      <div class="stats-card">
        <div class="stats-icons">
          <span>✅ {{ gameStore.correctCount }}</span>
          <span>❌ {{ gameStore.wrongCount }}</span>
        </div>
      </div>
    </div>

    <div v-if="gameStore.error" class="error-message">
      ⚠️ {{ gameStore.error }}
      <button @click="retryGame" class="retry-btn">Повторить</button>
    </div>

    <div v-if="gameStore.currentQuestion && !gameStore.isLoading" class="question-area">
      <div class="photo-container">
        <div class="photo-wrapper">
          <img 
            :src="gameStore.currentQuestion.photoUrl || 'https://via.placeholder.com/200x200/667eea/ffffff?text=👤'" 
            :alt="'Фото сотрудника'"
            @error="handleImageError"
            class="employee-photo"
          />
          <div class="photo-glow"></div>
        </div>
      </div>

      <h3 class="question-text">
        {{ gameMode === 'department' ? 'В каком отделе работает?' : 'Кто это?' }}
      </h3>

      <div class="options-grid">
        <button
          v-for="(option, index) in gameStore.currentQuestion.options"
          :key="index"
          @click="handleAnswer(index)"
          :disabled="gameStore.isLoading || !!gameStore.feedback"
          class="option-btn"
          :class="{ 'correct-answer': showCorrectAnswer && option === correctAnswerName }"
        >
          <span class="option-letter">{{ String.fromCharCode(65 + index) }}</span>
          <span class="option-text">{{ option }}</span>
        </button>
      </div>

      <div v-if="gameStore.feedback" class="feedback-overlay">
        <div class="feedback-card" :class="gameStore.feedback.isCorrect ? 'correct' : 'wrong'">
          <div class="feedback-icon">{{ gameStore.feedback.isCorrect ? '🎉' : '😢' }}</div>
          <div class="feedback-title">{{ gameStore.feedback.isCorrect ? 'ВЕРНО!' : 'ОШИБКА!' }}</div>
          <div class="feedback-points">{{ gameStore.feedback.isCorrect ? '+' : '' }}{{ gameStore.feedback.pointsDelta }} баллов</div>
          <div class="feedback-details" v-if="!gameStore.feedback.isCorrect">
            Правильный ответ: <strong>{{ gameStore.feedback.correctAnswer }}</strong>
          </div>
          <div class="feedback-streak" v-if="currentStreak > 0">
            🔥 Серия: {{ currentStreak }} правильных ответа подряд!
          </div>
          <div class="loading-next">Загрузка следующего вопроса...</div>
        </div>
      </div>
    </div>

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
import { useRouter } from 'vue-router';
import { computed, ref, watch, onMounted } from 'vue';
import { useGameStore } from '../stores/game';
import LoadingSpinner from './LoadingSpinner.vue';
import Mascot from './Mascot.vue';

const props = defineProps<{
  userId: number;
  chatId?: number;
  gameMode?: 'name' | 'department';
}>();

const emit = defineEmits<{
  'back-to-menu': [];
}>();

const router = useRouter();
const gameStore = useGameStore();
const currentStreak = computed(() => gameStore.currentStreak);
const showCorrectAnswer = ref(false);
const correctAnswerName = ref('');

const mascotMood = computed(() => {
  if (gameStore.feedback) {
    return gameStore.feedback.isCorrect ? 'happy' : 'sad';
  }
  if (gameStore.isLoading) return 'thinking';
  return 'neutral';
});

const mascotMessage = computed(() => {
  if (gameStore.feedback) {
    return gameStore.feedback.isCorrect 
      ? 'Отлично! Так держать! Продолжай в том же духе!' 
      : `Ничего страшного! Правильный ответ: ${gameStore.feedback.correctAnswer}`;
  }
  if (gameStore.isLoading && !gameStore.currentQuestion) return 'Загружаем первый вопрос...';
  if (gameStore.isLoading) return 'Думаю над следующим вопросом...';
  if (gameStore.score === 0 && !gameStore.currentQuestion) return 'Привет! Давай проверим, как хорошо ты знаешь коллег!';
  if (currentStreak.value >= 5) return 'Вау! У тебя невероятная серия! Так держать!';
  if (currentStreak.value >= 3) return 'Отличная серия! Продолжай в том же духе!';
  return 'Кто это на фото? Выбери правильный ответ!';
});

const handleAnswer = async (index: number) => {
  if (!gameStore.isLoading && !gameStore.feedback && gameStore.currentQuestion) {
    await gameStore.submitAnswer(index);
    
    if (gameStore.feedback?.isCorrect) {
      currentStreak.value++;
    } else {
      currentStreak.value = 0;
      showCorrectAnswer.value = true;
      correctAnswerName.value = gameStore.feedback?.correctAnswer || '';
      setTimeout(() => {
        showCorrectAnswer.value = false;
      }, 2000);
    }
  }
};

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.src = 'https://via.placeholder.com/200x200/667eea/ffffff?text=👤';
};

const retryGame = () => {
  gameStore.resetGame();
  gameStore.initGame(props.userId, props.chatId || props.userId);
};

const backToMenu = () => {
  console.log('🔙 Back to menu button clicked - resetting game');
  gameStore.resetGame();
  console.log('📤 Emitting back-to-menu event to parent');
  emit('back-to-menu');
  console.log('✅ Event emitted successfully');
};


// Инициализируем игру при монтировании компонента
onMounted(() => {
  console.log('GameBoard mounted, initializing game for user:', props.userId);
  gameStore.initGame(props.userId, props.chatId || props.userId);
});

// Следим за ошибками
watch(() => gameStore.error, (error) => {
  if (error) {
    console.error('GameStore error:', error);
  }
});

// Следим за вопросами
watch(() => gameStore.currentQuestion, (question) => {
  if (question) {
    console.log('New question loaded:', question);
  }
});
</script>

<style scoped>
.game-board {
  max-width: 600px;
  margin: 0 auto;
  background: white;
  border-radius: 30px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  animation: slideIn 0.5s ease;
}

@keyframes slideIn {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.score-header {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 15px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.score-card, .streak-card, .stats-card {
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
}

.score-icon, .streak-icon {
  font-size: 24px;
}

.score-info, .streak-info {
  flex: 1;
}

.score-label, .streak-label {
  font-size: 10px;
  opacity: 0.8;
}

.score-value, .streak-value {
  font-size: 24px;
  font-weight: bold;
  line-height: 1;
}

.stats-card {
  justify-content: center;
}

.stats-icons {
  display: flex;
  gap: 10px;
  font-size: 14px;
  font-weight: bold;
}

.question-area {
  padding: 30px;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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
  background: radial-gradient(circle, rgba(102,126,234,0.3) 0%, rgba(118,75,162,0) 70%);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.5;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.1);
    opacity: 0.8;
  }
}

.best-streak {
  font-size: 10px;
  opacity: 0.8;
  margin-top: 2px;
}

.streak-info {
  flex: 1;
}

.streak-label {
  font-size: 10px;
  opacity: 0.8;
}

.streak-value {
  font-size: 24px;
  font-weight: bold;
  line-height: 1;
}

.question-text {
  text-align: center;
  color: #667eea;
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
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
}

.option-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(102,126,234,0.2);
  border-color: #667eea;
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
  background: #667eea;
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
  color: #667eea;
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
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.feedback-card {
  background: white;
  padding: 30px;
  border-radius: 30px;
  text-align: center;
  max-width: 300px;
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

.feedback-card.correct {
  background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
  color: white;
}

.feedback-card.wrong {
  background: linear-gradient(135deg, #f44336 0%, #da190b 100%);
  color: white;
}

.feedback-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.feedback-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 10px;
}

.feedback-points {
  font-size: 20px;
  margin-bottom: 15px;
}

.feedback-details {
  margin: 15px 0;
  padding: 10px;
  background: rgba(255,255,255,0.2);
  border-radius: 10px;
}

.feedback-streak {
  margin: 15px 0;
  font-size: 14px;
}

.loading-next {
  margin-top: 15px;
  font-size: 12px;
  opacity: 0.8;
}

.error-message {
  background: #f44336;
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
  color: #f44336;
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
  border: 3px solid #f3f3f3;
  border-top: 3px solid #667eea;
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
  background: rgba(255,255,255,0.9);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  color: #667eea;
  transition: all 0.3s;
}

.menu-back-btn:hover {
  background: white;
  transform: translateY(-2px);
}

@media (max-width: 500px) {
  .options-grid {
    grid-template-columns: 1fr;
  }
  
  .employee-photo {
    width: 150px;
    height: 150px;
  }
  
  .question-text {
    font-size: 20px;
  }
}
</style>