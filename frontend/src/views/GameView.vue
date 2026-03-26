<template>
  <div class="game-view">
    <div class="game-container">
      <!-- Главное меню -->
      <div v-if="currentView === 'menu'" class="menu-wrapper">
        <div class="simple-menu">
          <div class="simple-menu-header">
            <div class="logo-icon">🎮</div>
            <h1 class="simple-title">Угадай коллегу</h1>
          </div>

          <!-- Отображение статистики в меню -->
          <div class="stats-preview" v-if="gameStore.totalQuestions > 0">
            <div class="preview-card">
              <div class="preview-value">{{ gameStore.score }}</div>
              <div class="preview-label">Всего очков</div>
            </div>
            <div class="preview-card">
              <div class="preview-value">{{ gameStore.accuracy }}%</div>
              <div class="preview-label">Точность</div>
            </div>
            <div class="preview-card">
              <div class="preview-value">{{ gameStore.bestStreak }}</div>
              <div class="preview-label">Лучшая серия</div>
            </div>
          </div>
          
          <div class="simple-buttons">
            <button @click="startGame" class="simple-btn primary">
              🎮 Начать игру
            </button>
            <button @click="showLeaderboard" class="simple-btn">
              🏆 Лидерборд
            </button>
            <button @click="showStats" class="simple-btn">
              📊 Моя статистика
            </button>
            <button v-if="isAdmin" @click="showAdmin" class="simple-btn admin">
              ⚙️ Админ-панель
            </button>
            <button @click="resetAllStats" class="simple-btn reset">
              🔄 Сбросить статистику
            </button>
          </div>
          
          <div class="simple-mascot">
            <div class="mascot-emoji">🐱</div>
            <div class="mascot-message">Готов проверить свои знания о коллегах? Начни игру прямо сейчас!</div>
          </div>
        </div>
      </div>
      
      <!-- Игровое поле -->
      <div v-if="currentView === 'game'" class="game-wrapper">
        <GameBoard 
          :key="gameKey"
          :userId="userId"
          :chatId="chatId"
          :gameMode="gameMode"
          @back-to-menu="handleBackToMenu"
        />
      </div>
      
      <!-- Лидерборд -->
      <div v-if="currentView === 'leaderboard'" class="leaderboard-wrapper">
        <Leaderboard
          :players="leaderboardData"
          :currentUserId="userId"
          :currentUserRank="currentUserRank"
          @close="currentView = 'menu'"
          @play="startGame"
          @show-stats="showStats"
        />
      </div>
      
      <!-- Статистика -->
      <div v-if="currentView === 'stats'" class="stats-wrapper">
        <PlayerStats
          @close="currentView = 'menu'"
          @play="startGame"
          @show-leaderboard="showLeaderboard"
        />
      </div>
      
      <!-- Админ-панель -->
      <div v-if="currentView === 'admin'" class="admin-wrapper">
        <AdminPanel
          @close="currentView = 'menu'"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useGameStore } from '../stores/game';
import GameBoard from '../components/GameBoard.vue';
import Leaderboard from '../components/Leaderboard.vue';
import PlayerStats from '../components/PlayerStats.vue';
import AdminPanel from '../components/AdminPanel.vue';

const gameStore = useGameStore();

// Состояние приложения
const currentView = ref<'menu' | 'game' | 'leaderboard' | 'stats' | 'admin'>('menu');
const userId = ref(0);
const chatId = ref(0);
const userName = ref('');
const gameMode = ref<'name' | 'department'>('name');
const isAdmin = ref(false);
const gameKey = ref(0);

// Данные для лидерборда (временно демо)
const leaderboardData = ref([
  { userId: 1, fullName: 'Анна Иванова', totalScore: 1250, accuracy: 85, rank: 1 },
  { userId: 2, fullName: 'Петр Сидоров', totalScore: 1120, accuracy: 78, rank: 2 },
  { userId: 3, fullName: 'Елена Козлова', totalScore: 980, accuracy: 72, rank: 3 },
]);

const currentUserRank = ref({
  rank: 24,
  totalScore: 425,
  toTop: 70
});

const startGame = () => {
  console.log('Starting game, current stats:', { 
    score: gameStore.score, 
    correct: gameStore.correctCount,
    wrong: gameStore.wrongCount 
  });
  gameKey.value++;
  currentView.value = 'game';
};

const handleBackToMenu = () => {
  console.log('Back to menu, stats preserved:', {
    score: gameStore.score,
    correct: gameStore.correctCount,
    wrong: gameStore.wrongCount
  });
  currentView.value = 'menu';
};

const showLeaderboard = () => {
  currentView.value = 'leaderboard';
};

const showStats = () => {
  currentView.value = 'stats';
};

const showAdmin = () => {
  currentView.value = 'admin';
};

const resetAllStats = () => {
  if (confirm('Вы уверены, что хотите сбросить всю статистику? Это действие нельзя отменить.')) {
    gameStore.resetStats();
    alert('Статистика сброшена!');
  }
};

onMounted(() => {
  console.log('GameView mounted');
  
  const telegram = (window as any).Telegram?.WebApp;
  if (telegram) {
    telegram.ready();
    telegram.expand();
    
    const initDataUnsafe = telegram.initDataUnsafe;
    if (initDataUnsafe && initDataUnsafe.user) {
      userId.value = initDataUnsafe.user.id;
      chatId.value = initDataUnsafe.user.id;
      userName.value = `${initDataUnsafe.user.first_name || ''} ${initDataUnsafe.user.last_name || ''}`.trim();
      isAdmin.value = initDataUnsafe.user.username === 'admin' || initDataUnsafe.user.id === 123456789;
    }
  } else {
    userId.value = 123456789;
    chatId.value = 123456789;
    userName.value = 'Тестовый Пользователь';
    isAdmin.value = true;
  }
});
</script>

<style scoped>
.game-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.game-container {
  max-width: 600px;
  margin: 0 auto;
}

.simple-menu {
  background: white;
  border-radius: 30px;
  padding: 40px 30px;
  text-align: center;
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

.simple-menu-header {
  margin-bottom: 30px;
}

.logo-icon {
  font-size: 60px;
  margin-bottom: 10px;
}

.simple-title {
  font-size: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.stats-preview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-bottom: 30px;
}

.preview-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 15px;
  border-radius: 15px;
  text-align: center;
  transition: transform 0.3s;
}

.preview-card:hover {
  transform: translateY(-3px);
}

.preview-value {
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
}

.preview-label {
  font-size: 11px;
  color: #666;
  margin-top: 5px;
}

.simple-buttons {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 30px;
}

.simple-btn {
  padding: 15px 25px;
  font-size: 16px;
  font-weight: bold;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f8f9fa;
  color: #667eea;
  text-align: center;
}

.simple-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(102,126,234,0.2);
}

.simple-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.simple-btn.admin {
  background: #f44336;
  color: white;
}

.simple-btn.reset {
  background: #ff9800;
  color: white;
}

.simple-mascot {
  margin-top: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 20px;
}

.mascot-emoji {
  font-size: 50px;
  margin-bottom: 10px;
}

.mascot-message {
  font-size: 14px;
  color: #666;
  font-style: italic;
}

.menu-wrapper,
.game-wrapper,
.leaderboard-wrapper,
.stats-wrapper,
.admin-wrapper {
  animation: fadeIn 0.3s ease;
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
</style>