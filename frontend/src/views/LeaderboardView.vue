<template>
  <div class="leaderboard-view">
    <Leaderboard
      :currentUserId="currentUserId"
      @close="$router.push('/')"
      @play="$router.push('/game')"
      @show-stats="$router.push('/stats')"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { GameStore } from '../stores/game';
import Leaderboard from '../components/leaderboard/Leaderboard.vue';

const gameStore = GameStore();
const currentUserId = computed(() => gameStore.userId ?? 0);
</script>

<style scoped>
.leaderboard-view {
  min-height: 100vh;
  background: #000000;
  padding: 20px;
}
</style>