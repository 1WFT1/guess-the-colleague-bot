<template>
  <div class="leaderboard-modal" @click.self="$emit('close')">
    <div class="leaderboard-content">
      <div class="leaderboard-header">
        <h2>🏆 Рейтинг игроков</h2>
        <button @click="$emit('close')" class="close-btn">✕</button>
      </div>
      
      <div class="leaderboard-tabs">
        <button 
          :class="['tab-btn', { active: activeTab === 'global' }]"
          @click="activeTab = 'global'"
        >
          🌍 Глобальный
        </button>
        <button 
          :class="['tab-btn', { active: activeTab === 'weekly' }]"
          @click="activeTab = 'weekly'"
        >
          📅 За неделю
        </button>
      </div>
      
      <div class="leaderboard-list">
        <div 
          v-for="(player, index) in displayedLeaderboard" 
          :key="player.userId"
          :class="['leaderboard-item', getLeaderboardItemClass(index, player.userId)]"
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
        
        <div v-if="displayedLeaderboard.length === 0" class="empty-state">
          <div class="empty-icon">🏆</div>
          <p>Пока нет игроков в рейтинге</p>
          <p class="empty-hint">Сыграйте первую игру, чтобы попасть в таблицу лидеров!</p>
        </div>
      </div>
      
      <div v-if="currentUser" class="current-user-rank">
        <div class="rank-badge">
          <span class="rank-icon">📍</span>
          ВАША ПОЗИЦИЯ: {{ currentUser.rank || '—' }} место
        </div>
        <div class="user-score">Ваши баллы: {{ currentUser.totalScore }}</div>
        <div v-if="currentUser.toTop && currentUser.toTop > 0" class="to-top">
          До топ-10 не хватает: {{ currentUser.toTop }} баллов
        </div>
        <div v-if="currentUser.toTop && currentUser.toTop > 0" class="progress-bar">
          <div class="progress-fill" :style="{ width: `${progressPercentage}%` }"></div>
        </div>
      </div>
      
      <Mascot :mood="mascotMood" :message="mascotMessage" />
      
      <div class="leaderboard-footer">
        <button @click="$emit('play')" class="action-btn">🎮 Играть</button>
        <button @click="$emit('show-stats')" class="action-btn secondary">📊 Моя статистика</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import Mascot from './Mascot.vue';
import { getInitials } from '../utils/formatters';

// Расширенный интерфейс игрока для лидерборда
interface LeaderboardPlayer {
  userId: number;
  fullName: string;
  totalScore: number;
  accuracy: number;
  rank?: number;
  correctCount?: number;
  wrongCount?: number;
  totalQuestions?: number;
}

interface CurrentUserWithTop extends LeaderboardPlayer {
  toTop: number;
}

const props = defineProps<{
  currentUserId: number;
}>();

const emit = defineEmits<{
  close: [];
  play: [];
  'show-stats': [];
}>();

const activeTab = ref<'global' | 'weekly'>('global');

// Функции для загрузки данных
const loadGlobalLeaderboard = (): LeaderboardPlayer[] => {
  const allPlayersKey = 'guess_colleague_all_players_v1';
  const saved = localStorage.getItem(allPlayersKey);
  
  if (saved) {
    try {
      const players = JSON.parse(saved);
      // Группируем по userId
      const userMap = new Map<number, LeaderboardPlayer>();
      
      players.forEach((player: any) => {
        const existing = userMap.get(player.userId);
        if (existing) {
          existing.totalScore += player.totalScore;
          // Обновляем счетчики вопросов
          existing.correctCount = (existing.correctCount || 0) + (player.correctCount || 0);
          existing.wrongCount = (existing.wrongCount || 0) + (player.wrongCount || 0);
          existing.totalQuestions = (existing.totalQuestions || 0) + (player.correctCount || 0) + (player.wrongCount || 0);
          // Пересчитываем точность
          const total = (existing.totalQuestions || 1);
          existing.accuracy = Math.round(((existing.correctCount || 0) / total) * 100);
        } else {
          const correctCount = player.correctCount || 0;
          const wrongCount = player.wrongCount || 0;
          const totalQuestions = correctCount + wrongCount;
          userMap.set(player.userId, {
            userId: player.userId,
            fullName: player.fullName || `Игрок ${player.userId}`,
            totalScore: player.totalScore,
            accuracy: player.accuracy || 0,
            correctCount: correctCount,
            wrongCount: wrongCount,
            totalQuestions: totalQuestions
          });
        }
      });
      
      const playersList = Array.from(userMap.values());
      playersList.sort((a, b) => b.totalScore - a.totalScore);
      return playersList.map((p, idx) => ({ ...p, rank: idx + 1 }));
    } catch (e) {
      console.error('Failed to load global leaderboard', e);
    }
  }
  return [];
};

const loadWeeklyLeaderboard = (): LeaderboardPlayer[] => {
  // Для недельного рейтинга используем данные из недельной статистики
  const weekKey = getCurrentWeekKey();
  const weeklyStatsKey = `weekly_stats_${weekKey}`;
  const saved = localStorage.getItem(weeklyStatsKey);
  
  if (saved) {
    try {
      const stats = JSON.parse(saved);
      if (stats.players) {
        return stats.players.sort((a: LeaderboardPlayer, b: LeaderboardPlayer) => b.totalScore - a.totalScore)
          .map((p: LeaderboardPlayer, idx: number) => ({ ...p, rank: idx + 1 }));
      }
    } catch (e) {
      console.error('Failed to load weekly leaderboard', e);
    }
  }
  return loadGlobalLeaderboard().slice(0, 50); // Fallback to global
};

const getCurrentWeekKey = (): string => {
  const now = new Date();
  const year = now.getFullYear();
  const weekNumber = getWeekNumber(now);
  return `${year}_${weekNumber}`;
};

const getWeekNumber = (date: Date): number => {
  const firstDayOfYear = new Date(date.getFullYear(), 0, 1);
  const pastDaysOfYear = (date.getTime() - firstDayOfYear.getTime()) / 86400000;
  return Math.ceil((pastDaysOfYear + firstDayOfYear.getDay() + 1) / 7);
};

const globalLeaderboard = ref<LeaderboardPlayer[]>([]);
const weeklyLeaderboard = ref<LeaderboardPlayer[]>([]);

const displayedLeaderboard = computed(() => {
  return activeTab.value === 'global' ? globalLeaderboard.value : weeklyLeaderboard.value;
});

const currentUser = computed<CurrentUserWithTop | null>(() => {
  const players = displayedLeaderboard.value;
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
  const topScore = displayedLeaderboard.value[0]?.totalScore || 1;
  return (currentUser.value.totalScore / topScore) * 100;
});

const mascotMood = computed(() => {
  if (currentUser.value && currentUser.value.rank && currentUser.value.rank <= 10) return 'happy';
  if (currentUser.value && currentUser.value.rank && currentUser.value.rank <= 20) return 'neutral';
  return 'thinking';
});

const mascotMessage = computed(() => {
  if (currentUser.value && currentUser.value.rank && currentUser.value.rank <= 10) {
    return 'Отличная работа! Ты в топ-10! Продолжай в том же духе!';
  } else if (currentUser.value && currentUser.value.rank && currentUser.value.rank <= 20) {
    return `Хороший результат! Ты в топ-20. До топ-10 не хватает ${currentUser.value.toTop} баллов!`;
  } else if (currentUser.value) {
    return `Попробуй сыграть еще раз и войди в топ-10! Не хватает ${currentUser.value.toTop} баллов!`;
  }
  return 'Соревнуйся с коллегами и попади в топ-10!';
});

const getLeaderboardItemClass = (index: number, userId: number): string => {
  const classes = [];
  if (index === 0) classes.push('top-1');
  if (index === 1) classes.push('top-2');
  if (index === 2) classes.push('top-3');
  if (userId === props.currentUserId) classes.push('is-current-user');
  return classes.join(' ');
};

const loadData = () => {
  globalLeaderboard.value = loadGlobalLeaderboard();
  weeklyLeaderboard.value = loadWeeklyLeaderboard();
};

onMounted(() => {
  loadData();
});
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
  from { transform: translateY(50px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
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

.leaderboard-header h2 { margin: 0; }

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: white;
  transition: transform 0.3s;
}

.close-btn:hover { transform: rotate(90deg); }

.leaderboard-tabs {
  display: flex;
  gap: 10px;
  padding: 15px 20px;
  background: #1a1a1a;
  border-bottom: 1px solid #2a2a2a;
}

.tab-btn {
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

.tab-btn.active {
  background: #4f4ff4;
  color: white;
  border-color: #4f4ff4;
}

.tab-btn:hover:not(.active) {
  background: #3a3a3a;
  color: #e0e0e0;
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

.player-info { flex: 1; }

.player-name {
  font-weight: bold;
  color: white;
  margin-bottom: 4px;
}

.player-stats {
  font-size: 12px;
  color: #888;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.games-count { color: #4f4ff4; }

.score {
  font-weight: bold;
  color: #4f4ff4;
  font-size: 18px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  color: #888;
  margin: 8px 0;
}

.empty-hint {
  font-size: 12px;
  color: #666;
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