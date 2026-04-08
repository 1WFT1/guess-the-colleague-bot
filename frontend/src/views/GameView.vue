<template>
  <div class="game-view">
    <div class="game-container">
        <div v-if="connectionStatus" class="connection-status" :class="connectionStatus.type">
          {{ connectionStatus.message }}
        </div>

      <div v-if="!isStatsLoaded" class="loading-screen">
        <div class="loader"></div>
        <p>Загрузка...</p>
      </div>
      
      <!-- Главное меню -->
      <div v-if="currentView === 'menu'" class="menu-wrapper">
        <div class="simple-menu">
          <div class="simple-menu-header">
            <h1 class="simple-title">Угадай коллегу</h1>
          </div>

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
          </div>
          
          <div class="simple-mascot">
            <img 
              src="/public/assets/images/codic_start.png" 
              alt="Маскот" 
              class="mascot-image"
              @error="handleImageError"
            />
            <div class="mascot-message">Готов проверить свои знания о коллегах? Начни игру прямо сейчас!</div>
          </div>
        </div>
      </div>
      
      <!-- Игровое поле -->
      <GameBoard 
        v-if="currentView === 'game'"
        :key="gameKey"
        :userId="userId"
        :chatId="chatId"
        :gameMode="gameMode"
        @back-to-menu="handleBackToMenu"
      />
      
      <!-- Лидерборд -->
      <Leaderboard
        v-if="currentView === 'leaderboard'"
        :currentUserId="userId"
        @close="currentView = 'menu'"
        @play="startGame"
        @show-stats="showStats"
      />
      
      <!-- Статистика -->
      <PlayerStats
        v-if="currentView === 'stats'"
        @close="currentView = 'menu'"
        @play="startGame"
        @show-leaderboard="showLeaderboard"
      />
      
      <!-- Админ панель -->
      <AdminPanel
        v-if="currentView === 'admin'"
        @close="currentView = 'menu'"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useGameStore } from '../stores/game';
import { useTelegram } from '../composables/useTelegram';
import GameBoard from '../components/GameBoard.vue';
import Leaderboard from '../components/Leaderboard.vue';
import PlayerStats from '../components/PlayerStats.vue';
import AdminPanel from '../components/AdminPanel.vue';

const connectionStatus = ref<{ type: string; message: string } | null>(null);

const showConnectionStatus = (type: 'error' | 'success' | 'info', message: string) => {
  connectionStatus.value = { type, message };
  setTimeout(() => {
    connectionStatus.value = null;
  }, 5000);
};
// Проверка подключения к бэкенду
const checkBackendConnection = async () => {
  showConnectionStatus('info', 'Проверка подключения к серверу...');
  
  try {
    const apiUrl = 'https://b434ebb165dc5c64-178-68-29-212.serveousercontent.com/api';
    const response = await fetch(`${apiUrl}/game/session?userId=${userId.value}&chatId=${chatId.value}`, {
      method: 'POST'
    });
    
    if (response.ok) {
      showConnectionStatus('success', '✅ Подключение к серверу установлено');
      return true;
    } else {
      showConnectionStatus('error', `❌ Ошибка сервера: ${response.status}`);
      return false;
    }
  } catch (err: any) {
    showConnectionStatus('error', `❌ Не удалось подключиться к серверу: ${err.message}`);
    return false;
  }
};

const gameStore = useGameStore();
const { userId, isAdmin, userName } = useTelegram();

const currentView = ref<'menu' | 'game' | 'leaderboard' | 'stats' | 'admin'>('menu');
const chatId = ref(0);
const gameMode = ref<'name' | 'department'>('name');
const gameKey = ref(0);
const isStatsLoaded = ref(false);

const showError = (message: string) => {
  if ((window as any).showError) {
    (window as any).showError(message);
  }
  console.error(message);
};


// Загрузка статистики пользователя с бэкенда
const loadUserStats = async () => {
  if (!userId.value) return;
  
  try {
    console.log('Loading user stats from backend...');
    const success = await gameStore.loadStatsFromBackend();
    if (success) {
      console.log('User stats loaded:', {
        score: gameStore.score,
        accuracy: gameStore.accuracy,
        bestStreak: gameStore.bestStreak
      });
    } else {
      console.log('Failed to load stats, using defaults');
    }
  } catch (error) {
    console.error('Failed to load user stats:', error);
  } finally {
    isStatsLoaded.value = true;
  }
};

// Получение данных пользователя из Telegram
const getTelegramUserData = () => {
  const telegram = (window as any).Telegram?.WebApp;
  if (telegram?.initDataUnsafe?.user) {
    const user = telegram.initDataUnsafe.user;
    return {
      username: user.username || '',
      firstName: user.first_name || '',
      lastName: user.last_name || ''
    };
  }
  return {
    username: 'test_user',
    firstName: 'Тестовый',
    lastName: 'Пользователь'
  };
};

// Также добавьте тип для Vite env
declare global {
  interface ImportMeta {
    env: {
      VITE_API_URL?: string;
      [key: string]: string | undefined;
    };
  }
}

const startGame = async () => {
  console.log('🎮 Starting game...');
  
  // Сохраняем имя пользователя
  localStorage.setItem('userName', userName.value);
  
  // ТОЛЬКО ОДИН РАЗ - через store
  await gameStore.initGame(userId.value, userId.value);
  
  // Переключаемся на игровое поле
  gameKey.value++;
  currentView.value = 'game';
};

  const handleBackToMenu = async () => {
    console.log('🔙 Back to menu from game');
    currentView.value = 'menu';
    // Принудительно обновляем статистику
    await loadUserStats();
    // Принудительно обновляем отображение
    gameKey.value++;
  };

const showLeaderboard = () => {
  console.log('🏆 Showing leaderboard');
  currentView.value = 'leaderboard';
};

const showStats = () => {
  console.log('📊 Showing player stats');
  currentView.value = 'stats';
};

const showAdmin = () => {
  console.log('⚙️ Showing admin panel');
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
    fallback.style.fontSize = '60px';
    fallback.style.animation = 'float 3s ease-in-out infinite';
    parent.insertBefore(fallback, img);
  }
};

onMounted(async () => {
  console.log('GameView mounted');
  console.log('User ID:', userId.value);
  console.log('Is Admin:', isAdmin.value);
  console.log('User Name:', userName.value);
  
  chatId.value = userId.value;
  
  if (userName.value) {
    localStorage.setItem('userName', userName.value);
  }
  
  // Показываем загрузку
  isStatsLoaded.value = false;
  
  // Загружаем статистику
  await loadUserStats();
  
  await checkBackendConnection();
  // Даем время на отрисовку
  await new Promise(resolve => setTimeout(resolve, 100));
});
</script>


<style scoped>


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

.simple-mascot {
  margin-top: 20px;
  padding: 20px;
  background: #2a2a2a;
  border-radius: 20px;
  border: 1px solid #3a3a3a;
  text-align: center;
}

.mascot-image {
  width: 150px;
  height: 150px;
  object-fit: contain;
  margin-bottom: 10px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.mascot-emoji-fallback {
  font-size: 60px;
  margin-bottom: 10px;
  animation: float 3s ease-in-out infinite;
  display: inline-block;
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
