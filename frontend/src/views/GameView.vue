<template>
  <div class="game-view">
    <div class="game-container">
      <!-- Главное меню -->
      <div v-if="currentView === 'menu'" class="menu-wrapper">
        <div class="simple-menu">
          <div class="simple-menu-header">
            <h1 class="simple-title">Угадай коллегу</h1>
          </div>

          <!-- Отображение статистики в меню - всегда показываем, даже если 0 -->
          <div class="stats-preview">
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
              Начать игру
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
          </div>
          
          <div class="simple-mascot">
            <img 
              src="C:\Users\user\IdeaProjects\guess-the-colleague-bot\frontend\src\assets\images\codic_start.png" 
              alt="Маскот" 
              class="mascot-image"
              @error="handleImageError"
            />
            <div class="mascot-message">Готов проверить свои знания о коллегах? Начни игру прямо сейчас!</div>
          </div>
        </div>
      </div>
      
      <!-- Остальные вьюхи без изменений -->
      <div v-if="currentView === 'game'" class="game-wrapper">
        <GameBoard 
          :key="gameKey"
          :userId="userId"
          :chatId="chatId"
          :gameMode="gameMode"
          @back-to-menu="handleBackToMenu"
        />
      </div>
      
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
      
      <div v-if="currentView === 'stats'" class="stats-wrapper">
        <PlayerStats
          @close="currentView = 'menu'"
          @play="startGame"
          @show-leaderboard="showLeaderboard"
        />
      </div>
      
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

// Реальные данные для лидерборда из localStorage
const leaderboardData = computed(() => {
  const allStats = localStorage.getItem('allPlayersStats');
  if (allStats) {
    try {
      const players = JSON.parse(allStats);
      // Добавляем текущего игрока
      const currentStats = localStorage.getItem('gameStats');
      if (currentStats) {
        const stats = JSON.parse(currentStats);
        const currentPlayer = {
          userId: userId.value,
          fullName: userName.value || 'Вы',
          totalScore: stats.score || 0,
          accuracy: Math.round((stats.correctCount / (stats.correctCount + stats.wrongCount || 1)) * 100)
        };
        
        // Обновляем или добавляем текущего игрока
        const existingIndex = players.findIndex((p: any) => p.userId === userId.value);
        if (existingIndex !== -1) {
          players[existingIndex] = currentPlayer;
        } else {
          players.push(currentPlayer);
        }
      }
      
      // Сортируем по очкам
      players.sort((a: any, b: any) => b.totalScore - a.totalScore);
      
      // Добавляем ранги
      players.forEach((p: any, idx: number) => {
        p.rank = idx + 1;
      });
      
      return players;
    } catch (e) {
      console.error('Failed to load leaderboard', e);
    }
  }
  return [];
});


// Реальная позиция текущего пользователя
const currentUserRank = computed(() => {
  const players = leaderboardData.value;
  const currentPlayer = players.find((p: any) => p.userId === userId.value);
  
  if (currentPlayer) {
    const top10Score = players[9]?.totalScore || 0;
    const toTop = top10Score - (currentPlayer.totalScore || 0);
    
    return {
      rank: currentPlayer.rank,
      totalScore: currentPlayer.totalScore,
      toTop: toTop > 0 ? toTop : 0
    };
  }
  
  return {
    rank: 1,
    totalScore: 0,
    toTop: 0
  };
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


const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.style.display = 'none';
  const parent = img.parentElement;
  if (parent) {
    const fallback = document.createElement('div');
    fallback.className = 'mascot-emoji-fallback';
    fallback.textContent = '🐱';
    parent.insertBefore(fallback, img);
  }
};


onMounted(() => {
  console.log('GameView mounted, current stats from store:', {
    score: gameStore.score,
    correct: gameStore.correctCount,
    accuracy: gameStore.accuracy
  });
  
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
/* Стили остаются те же (темная тема) */
.game-view {
  min-height: 100vh;
  background: #000000;
  padding: 20px;
}

.game-container {
  max-width: 600px;
  margin: 0 auto;
}

.simple-menu {
  background: #1a1a1a;
  border-radius: 30px;
  padding: 40px 30px;
  text-align: center;
  animation: slideIn 0.5s ease;
  border: 1px solid #2a2a2a;
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
  background: linear-gradient(135deg, #4f4ff4 0%, #6c6cff 100%);
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
  background: #2a2a2a;
  padding: 15px;
  border-radius: 15px;
  text-align: center;
  transition: transform 0.3s;
  border: 1px solid #3a3a3a;
}

.preview-card:hover {
  transform: translateY(-3px);
  border-color: #4f4ff4;
}

.preview-value {
  font-size: 24px;
  font-weight: bold;
  color: #4f4ff4;
}

.preview-label {
  font-size: 11px;
  color: #888;
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
  background: #2a2a2a;
  color: #ffffff;
  text-align: center;
  border: 1px solid #3a3a3a;
}

.simple-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(79, 79, 244, 0.2);
  border-color: #4f4ff4;
}

.simple-btn.primary {
  background: #4f4ff4;
  color: white;
  border: none;
}

.simple-btn.primary:hover {
  background: #6c6cff;
  box-shadow: 0 10px 20px rgba(79, 79, 244, 0.3);
}

.simple-btn.admin {
  background: #dc2626;
  color: white;
  border: none;
}

.simple-btn.admin:hover {
  background: #ef4444;
}

.simple-btn.reset {
  background: #2a2a2a;
  color: #ff9800;
  border: 1px solid #ff9800;
}

.simple-btn.reset:hover {
  background: #ff9800;
  color: white;
}

.simple-mascot {
  margin-top: 20px;
  padding: 20px;
  background: #2a2a2a;
  border-radius: 20px;
  border: 1px solid #3a3a3a;
  text-align: center;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.mascot-image {
  width: 150px;
  height: 150px;
  object-fit: contain;
  margin-bottom: 10px;
  animation: float 3s ease-in-out infinite;
}

.mascot-emoji-fallback {
  font-size: 60px;
  margin-bottom: 10px;
}

.mascot-message {
  font-size: 14px;
  color: #888;
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