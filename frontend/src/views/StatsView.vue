<template>
  <div class="stats-view">
    <PlayerStats
      :stats="playerStats"
      @close="$router.push('/')"
      @play="$router.push('/game')"
      @show-leaderboard="$router.push('/leaderboard')"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { GameStore } from '../stores/game';
import PlayerStats from '../components/stats/PlayerStats.vue';

const gameStore = GameStore();

const playerStats = computed(() => ({
  totalScore: gameStore.score,
  totalQuestions: gameStore.totalQuestions,
  correctAnswers: gameStore.correctCount,
  wrongAnswers: gameStore.wrongCount,
  bestStreak: gameStore.bestStreak,
  currentStreak: gameStore.currentStreak,
  weeklyStats: JSON.parse(localStorage.getItem('weeklyStats') || '{}')
}));
</script>

<style scoped>
.stats-view {
  min-height: 100vh;
  background: #000000;
  padding: 20px;
}
</style>