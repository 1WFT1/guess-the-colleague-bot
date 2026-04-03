<template>
  <div class="leaderboard-view">
    <Leaderboard
      :players="leaderboardData"
      :currentUserId="currentUserId"
      :currentUserRank="currentUserRank"
      @close="$router.push('/')"
      @play="$router.push('/game')"
      @show-stats="$router.push('/stats')"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGameStore } from '../stores/game';
import Leaderboard from '../components/Leaderboard.vue';

const gameStore = useGameStore();

// Исправление: преобразуем null в 0
const currentUserId = computed(() => gameStore.userId ?? 0);

// Реальные данные из localStorage
const leaderboardData = computed(() => {
  const allStats = localStorage.getItem('allPlayersStats');
  if (allStats) {
    try {
      return JSON.parse(allStats);
    } catch (e) {
      return [];
    }
  }
  return [];
});

const currentUserRank = computed(() => {
  return {
    rank: 1,
    totalScore: gameStore.score,
    toTop: 0
  };
});
</script>

<style scoped>
.leaderboard-view {
  min-height: 100vh;
  background: #000000;
  padding: 20px;
}
</style>