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
import { ref } from 'vue';
import PlayerStats from '../components/PlayerStats.vue';

const playerStats = ref({
  totalScore: 425,
  totalQuestions: 87,
  correctAnswers: 51,
  wrongAnswers: 36,
  bestStreak: 12,
  currentStreak: 3,
  weeklyStats: {
    'Пн': 50, 'Вт': 85, 'Ср': 25, 'Чт': 120, 'Пт': 145, 'Сб': 60, 'Вс': 40
  }
});
</script>

<style scoped>
.stats-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}
</style>