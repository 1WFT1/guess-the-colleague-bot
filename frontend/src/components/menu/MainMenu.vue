<template>
  <div class="main-menu">
    <div class="menu-header">
      <div class="logo">
        <div class="logo-icon">🎮</div>
        <h1 class="game-title">Угадай коллегу</h1>
      </div>
      <div class="user-info" v-if="userName">
        <div class="user-avatar">{{ userInitials }}</div>
        <div class="user-name">{{ userName }}</div>
      </div>
    </div>
    
    <div class="stats-preview" v-if="userStats">
      <div class="preview-card">
        <div class="preview-value">{{ userStats.totalScore }}</div>
        <div class="preview-label">Всего очков</div>
      </div>
      <div class="preview-card">
        <div class="preview-value">{{ userStats.accuracy }}%</div>
        <div class="preview-label">Точность</div>
      </div>
      <div class="preview-card">
        <div class="preview-value">{{ userStats.bestStreak }}</div>
        <div class="preview-label">Лучшая серия</div>
      </div>
    </div>
    
    <div class="menu-buttons">
      <button @click="$emit('start-game')" class="menu-btn primary">
        <span class="btn-icon">🎮</span>
        <span class="btn-text">Начать игру</span>
      </button>
      <button @click="$emit('show-leaderboard')" class="menu-btn">
        <span class="btn-icon">🏆</span>
        <span class="btn-text">Лидерборд</span>
      </button>
      <button @click="$emit('show-stats')" class="menu-btn">
        <span class="btn-icon">📊</span>
        <span class="btn-text">Моя статистика</span>
      </button>
      <button v-if="isAdmin" @click="$emit('show-admin')" class="menu-btn admin">
        <span class="btn-icon">⚙️</span>
        <span class="btn-text">Админ-панель</span>
      </button>
    </div>
    
    <div class="daily-challenge" v-if="dailyChallenge">
      <div class="challenge-header">
        <span class="challenge-icon">⭐</span>
        <span>Ежедневный вызов</span>
      </div>
      <div class="challenge-text">{{ dailyChallenge.text }}</div>
      <div class="challenge-reward">Награда: +{{ dailyChallenge.reward }} баллов</div>
    </div>
    
    <Mascot mood="neutral" :message="mascotMessage" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Mascot from './../common/Mascot.vue';

const props = defineProps<{
  isAdmin?: boolean;
  userName?: string;
  userStats?: {
    totalScore: number;
    accuracy: number;
    bestStreak: number;
  };
  dailyChallenge?: {
    text: string;
    reward: number;
  };
}>();

const emit = defineEmits<{
  'start-game': [];
  'show-leaderboard': [];
  'show-stats': [];
  'show-admin': [];
}>();

const userInitials = computed(() => {
  if (!props.userName || props.userName.length === 0) {
    return '👤';
  }
  
  const names = props.userName.trim().split(' ');
  
  // Проверяем, что есть хотя бы одно имя
  if (names.length >= 1 && names[0]) {
    const firstInitial = names[0][0];
    
    // Если есть фамилия
    if (names.length >= 2 && names[1]) {
      const secondInitial = names[1][0];
      if (firstInitial && secondInitial) {
        return (firstInitial + secondInitial).toUpperCase();
      }
    }
    
    // Если только имя
    if (firstInitial) {
      return firstInitial.toUpperCase();
    }
  }
  
  return '👤';
});

const mascotMessage = computed(() => {
  if (props.userStats && props.userStats.bestStreak > 10) {
    return 'Ты настоящий профессионал! Продолжай удивлять!';
  }
  if (props.userStats && props.userStats.totalScore > 1000) {
    return 'Отличные результаты! Ты в топе игроков!';
  }
  return 'Готов проверить свои знания о коллегах? Начни игру прямо сейчас!';
});
</script>

<style scoped>
.main-menu {
  max-width: 500px;
  margin: 0 auto;
  background: white;
  border-radius: 30px;
  padding: 30px;
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

.menu-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  margin-bottom: 20px;
}

.logo-icon {
  font-size: 60px;
  margin-bottom: 10px;
}

.game-title {
  font-size: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.user-info {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-top: 15px;
  padding: 10px 20px;
  background: #f8f9fa;
  border-radius: 50px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 18px;
}

.user-name {
  font-weight: 500;
  color: #333;
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

.menu-buttons {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 30px;
}

.menu-btn {
  padding: 15px 25px;
  font-size: 16px;
  font-weight: bold;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f8f9fa;
  color: #667eea;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.menu-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(102,126,234,0.2);
}

.menu-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 5px 15px rgba(102,126,234,0.3);
}

.menu-btn.admin {
  background: #f44336;
  color: white;
}

.btn-icon {
  font-size: 24px;
}

.btn-text {
  flex: 1;
  text-align: left;
}

.daily-challenge {
  background: linear-gradient(135deg, #ff9800 0%, #ff5722 100%);
  padding: 15px;
  border-radius: 15px;
  color: white;
  margin-bottom: 30px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
  }
}

.challenge-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-weight: bold;
}

.challenge-icon {
  font-size: 20px;
}

.challenge-text {
  font-size: 14px;
  margin-bottom: 8px;
}

.challenge-reward {
  font-size: 12px;
  opacity: 0.9;
}

@media (max-width: 500px) {
  .main-menu {
    padding: 20px;
  }
  
  .game-title {
    font-size: 24px;
  }
  
  .preview-value {
    font-size: 20px;
  }
}
</style>