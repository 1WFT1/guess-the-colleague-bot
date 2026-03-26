<template>
  <div class="leaderboard-modal" @click.self="$emit('close')">
    <div class="leaderboard-content">
      <div class="leaderboard-header">
        <h2>🏆 Рейтинг игроков</h2>
        <button @click="$emit('close')" class="close-btn">✕</button>
      </div>
      
      <div class="leaderboard-filters">
        <div class="filter-buttons">
          <button 
            v-for="period in periods" 
            :key="period.value"
            @click="selectedPeriod = period.value"
            :class="{ active: selectedPeriod === period.value }"
            class="filter-btn"
          >
            {{ period.label }}
          </button>
        </div>
        <div class="search-box">
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="Поиск по имени..."
            class="search-input"
          />
        </div>
      </div>
      
      <div class="leaderboard-list">
        <div 
          v-for="(player, index) in filteredPlayers" 
          :key="player.userId"
          :class="['leaderboard-item', { 
            'top-1': player.rank === 1,
            'top-2': player.rank === 2,
            'top-3': player.rank === 3,
            'is-current-user': player.userId === currentUserId 
          }]"
        >
          <div class="rank">
            <span v-if="player.rank === 1">🥇</span>
            <span v-else-if="player.rank === 2">🥈</span>
            <span v-else-if="player.rank === 3">🥉</span>
            <span v-else>{{ player.rank }}</span>
          </div>
          <div class="player-avatar">{{ player.fullName.charAt(0) }}</div>
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
      
      <div v-if="currentUserRank" class="current-user-rank">
        <div class="rank-badge">
          <span class="rank-icon">📍</span>
          ВАША ПОЗИЦИЯ: {{ currentUserRank.rank }} место
        </div>
        <div class="user-score">Ваши баллы: {{ currentUserRank.totalScore }}</div>
        <div class="to-top" v-if="currentUserRank.toTop !== undefined">
          До топ-10 не хватает: {{ currentUserRank.toTop }} баллов
        </div>
        <div class="progress-bar" v-if="currentUserRank.toTop !== undefined">
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
import { ref, computed } from 'vue';
import Mascot from './Mascot.vue';

interface LeaderboardPlayer {
  userId: number;
  fullName: string;
  totalScore: number;
  accuracy: number;
  rank?: number;
}

const props = defineProps<{
  players: LeaderboardPlayer[];
  currentUserId: number;
  currentUserRank?: {
    rank: number;
    totalScore: number;
    toTop?: number;
  };
}>();

const periods = [
  { label: 'За всё время', value: 'all' },
  { label: 'Эта неделя', value: 'week' },
  { label: 'Этот месяц', value: 'month' }
];

const selectedPeriod = ref('all');
const searchQuery = ref('');

const filteredPlayers = computed(() => {
  let filtered = [...props.players];
  
  if (searchQuery.value) {
    filtered = filtered.filter(p => 
      p.fullName.toLowerCase().includes(searchQuery.value.toLowerCase())
    );
  }
  
  return filtered.slice(0, 50);
});

const progressPercentage = computed(() => {
  if (!props.currentUserRank?.toTop) return 0;
  const topScore = props.players[0]?.totalScore || 1250;
  const currentScore = props.currentUserRank.totalScore;
  return (currentScore / topScore) * 100;
});

const mascotMessage = computed(() => {
  if (props.currentUserRank && props.currentUserRank.rank <= 10) {
    return 'Отличная работа! Ты в топ-10! Продолжай в том же духе!';
  } else if (props.currentUserRank) {
    const needed = props.currentUserRank.toTop || 70;
    return `Попробуй сыграть еще раз и войди в топ-10! Не хватает ${needed} баллов!`;
  }
  return 'Соревнуйся с коллегами и попади в топ-10!';
});

defineEmits<{
  close: [];
  play: [];
  'show-stats': [];
}>();
</script>

<style scoped>
.leaderboard-modal {
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

.leaderboard-content {
  background: white;
  border-radius: 30px;
  max-width: 700px;
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

.leaderboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

.leaderboard-filters {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.filter-buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.filter-btn {
  flex: 1;
  padding: 8px;
  background: #f0f0f0;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-btn.active {
  background: #667eea;
  color: white;
}

.search-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  font-size: 14px;
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
  background: #f8f9fa;
  border-radius: 15px;
  transition: all 0.3s;
}

.leaderboard-item:hover {
  transform: translateX(5px);
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.leaderboard-item.top-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
}

.leaderboard-item.top-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
}

.leaderboard-item.top-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #e8a75e 100%);
}

.leaderboard-item.is-current-user {
  background: #e3f2fd;
  border: 2px solid #667eea;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  color: #333;
  margin-bottom: 4px;
}

.player-stats {
  font-size: 12px;
  color: #666;
}

.player-stats span {
  margin-right: 5px;
}

.score {
  font-weight: bold;
  color: #667eea;
  font-size: 18px;
}

.current-user-rank {
  margin: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 15px;
  color: white;
  text-align: center;
}

.rank-badge {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.rank-icon {
  font-size: 20px;
}

.user-score {
  margin-bottom: 10px;
}

.progress-bar {
  margin-top: 15px;
  height: 8px;
  background: rgba(255,255,255,0.3);
  border-radius: 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: white;
  border-radius: 10px;
  transition: width 0.3s;
}

.leaderboard-footer {
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