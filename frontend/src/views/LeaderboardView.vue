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
import { ref } from 'vue';
import Leaderboard from '../components/Leaderboard.vue';

const currentUserId = ref(123456789);

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
</script>

<style scoped>
.leaderboard-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}
</style>