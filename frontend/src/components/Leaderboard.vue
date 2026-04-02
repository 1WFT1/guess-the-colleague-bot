<template>
  <div class="leaderboard-modal" @click.self="$emit('close')">
    <div class="leaderboard-content">
      <div class="leaderboard-header">
        <h2>🏆 Рейтинг игроков</h2>
        <button @click="$emit('close')" class="close-btn">✕</button>
      </div>
      
      <div class="leaderboard-list">
        <div 
          v-for="(player, index) in leaderboard" 
          :key="player.userId"
          :class="['leaderboard-item', { 
            'top-1': index === 0,
            'top-2': index === 1,
            'top-3': index === 2,
            'is-current-user': player.userId === currentUserId 
          }]"
        >
          <div class="rank">
            <span v-if="index === 0">🥇</span>
            <span v-else-if="index === 1">🥈</span>
            <span v-else-if="index === 2">🥉</span>
            <span v-else>{{ index + 1 }}</span>
          </div>
          <div class="player-avatar">{{ getInitials(player.fullName) }}</div>
          <div class="player-info">
            <div class="player-name">{{ player.fullName }}</div>
            <div class="player-stats">
              <span>{{ player.totalScore }} баллов</span>
              <span>•</span>
              <span>Точность: {{ player.accuracy }}%</span>
            </div>
          </div>
          <div class="score">{{ player.totalScore }}</div>
        </div>
      </div>
      
      <div v-if="currentUser" class="current-user-rank">
        <div class="rank-badge">
          <span class="rank-icon">📍</span>
          ВАША ПОЗИЦИЯ: {{ currentUser.rank }} место
        </div>
        <div class="user-score">Ваши баллы: {{ currentUser.totalScore }}</div>
        <div class="to-top" v-if="currentUser.toTop && currentUser.toTop > 0">
          До топ-10 не хватает: {{ currentUser.toTop }} баллов
        </div>
        <div class="progress-bar" v-if="currentUser.toTop && currentUser.toTop > 0">
          <div class="progress-fill" :style="{ width: `${progressPercentage}%` }"></div>
        </div>
      </div>
      
      <Mascot mood="thinking" :message="mascotMessage" />
      
      <div class="leaderboard-footer">
        <button @click="$emit('play')" class="action-btn">🎮 Играть</button>
        <button @click="$emit('show-stats')" class="action-btn secondary">📊 Моя статистика</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGameStore } from '../stores/game';
import Mascot from './Mascot.vue';

const gameStore = useGameStore();

const props = defineProps<{
  currentUserId: number;
}>();

const emit = defineEmits<{
  close: [];
  play: [];
  'show-stats': [];
}>();

// Получаем всех игроков из localStorage
const getAllPlayers = () => {
  const players: any[] = [];
  
  // Загружаем сохраненную статистику текущего игрока
  const currentStats = localStorage.getItem('gameStats');
  if (currentStats) {
    try {
      const stats = JSON.parse(currentStats);
      players.push({
        userId: props.currentUserId,
        fullName: getUserName(),
        totalScore: stats.score || 0,
        accuracy: Math.round((stats.correctCount / (stats.correctCount + stats.wrongCount || 1)) * 100),
        rank: 0
      });
    } catch (e) {}
  }
  
  // Загружаем других игроков из сохраненных данных
  const allStats = localStorage.getItem('allPlayersStats');
  if (allStats) {
    try {
      const savedPlayers = JSON.parse(allStats);
      savedPlayers.forEach((p: any) => {
        if (p.userId !== props.currentUserId) {
          players.push(p);
        }
      });
    } catch (e) {}
  }
  
  // Сортируем по очкам
  players.sort((a, b) => b.totalScore - a.totalScore);
  
  // Добавляем ранги
  players.forEach((p, idx) => {
    p.rank = idx + 1;
  });
  
  return players;
};

// Сохраняем статистику игрока
const savePlayerStats = () => {
  const currentStats = localStorage.getItem('gameStats');
  if (currentStats) {
    try {
      const stats = JSON.parse(currentStats);
      const allPlayers = getAllPlayers();
      
      // Обновляем текущего игрока
      const existingIndex = allPlayers.findIndex((p: any) => p.userId === props.currentUserId);
      if (existingIndex !== -1) {
        allPlayers[existingIndex] = {
          userId: props.currentUserId,
          fullName: getUserName(),
          totalScore: stats.score || 0,
          accuracy: Math.round((stats.correctCount / (stats.correctCount + stats.wrongCount || 1)) * 100)
        };
      }
      
      // Сохраняем
      localStorage.setItem('allPlayersStats', JSON.stringify(allPlayers));
    } catch (e) {}
  }
};

const getUserName = () => {
  const telegram = (window as any).Telegram?.WebApp;
  if (telegram && telegram.initDataUnsafe?.user) {
    const user = telegram.initDataUnsafe.user;
    return `${user.first_name || ''} ${user.last_name || ''}`.trim() || 'Игрок';
  }
  return 'Тестовый Пользователь';
};

const getInitials = (name: string) => {
  const parts = name.split(' ');
  if (parts.length >= 2) {
    return (parts[0][0] + parts[1][0]).toUpperCase();
  }
  return name[0]?.toUpperCase() || 'U';
};

const leaderboard = computed(() => {
  const players = getAllPlayers();
  return players.slice(0, 50);
});

const currentUser = computed(() => {
  const players = getAllPlayers();
  const user = players.find(p => p.userId === props.currentUserId);
  if (user) {
    const top10Score = players[9]?.totalScore || 0;
    const toTop = top10Score - user.totalScore;
    return {
      ...user,
      toTop: toTop > 0 ? toTop : 0
    };
  }
  return null;
});

const progressPercentage = computed(() => {
  if (!currentUser.value) return 0;
  const topScore = leaderboard.value[0]?.totalScore || 1;
  return (currentUser.value.totalScore / topScore) * 100;
});

const mascotMessage = computed(() => {
  if (currentUser.value && currentUser.value.rank <= 10) {
    return 'Отличная работа! Ты в топ-10! Продолжай в том же духе!';
  } else if (currentUser.value) {
    return `Попробуй сыграть еще раз и войди в топ-10! Не хватает ${currentUser.value.toTop} баллов!`;
  }
  return 'Соревнуйся с коллегами и попади в топ-10!';
});

// Сохраняем статистику при загрузке
savePlayerStats();
</script>

<style scoped>
.leaderboard-modal {
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

.leaderboard-content {
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
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.leaderboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #2a2a2a;
  background: linear-gradient(135deg, #4f4ff4 0%, #6c6cff 100%);
  color: white;
  border-radius: 30px 30px 0 0;
}

.leaderboard-header h2 {
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

.leaderboard-list {
  padding: 20px;
  max-height: 400px;
  overflow-y: auto;
}

.leaderboard-item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 8px;
  background: #2a2a2a;
  border-radius: 15px;
  transition: all 0.3s;
  border: 1px solid #3a3a3a;
}

.leaderboard-item:hover {
  transform: translateX(5px);
  border-color: #4f4ff4;
}

.leaderboard-item.top-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  border: none;
}

.leaderboard-item.top-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  border: none;
}

.leaderboard-item.top-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #e8a75e 100%);
  border: none;
}

.leaderboard-item.top-1 .player-name,
.leaderboard-item.top-2 .player-name,
.leaderboard-item.top-3 .player-name {
  color: #ffffff;
}

.leaderboard-item.top-1 .score,
.leaderboard-item.top-2 .score,
.leaderboard-item.top-3 .score {
  color: #e8a75e;
}

.leaderboard-item.is-current-user {
  background: #1a2a3a;
  border: 2px solid #4f4ff4;
}

.rank {
  width: 60px;
  font-size: 24px;
  font-weight: bold;
  text-align: center;
}

.player-avatar {
  width: 45px;
  height: 45px;
  background: linear-gradient(135deg, #4f4ff4 0%, #6c6cff 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 18px;
  margin-right: 15px;
}

.player-info {
  flex: 1;
}

.player-name {
  font-weight: bold;
  color: white;
  margin-bottom: 4px;
}

.player-stats {
  font-size: 12px;
  color: #888;
}

.score {
  font-weight: bold;
  color: #4f4ff4;
  font-size: 18px;
}

.current-user-rank {
  margin: 20px;
  padding: 20px;
  background: #2a2a2a;
  border-radius: 15px;
  text-align: center;
  border: 1px solid #4f4ff4;
}

.rank-badge {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  color: white;
}

.user-score {
  margin-bottom: 10px;
  color: #888;
}

.to-top {
  color: #ff9800;
  font-size: 14px;
}

.progress-bar {
  margin-top: 15px;
  height: 8px;
  background: rgba(79, 79, 244, 0.2);
  border-radius: 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #4f4ff4;
  border-radius: 10px;
  transition: width 0.3s;
}

.leaderboard-footer {
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
</style>