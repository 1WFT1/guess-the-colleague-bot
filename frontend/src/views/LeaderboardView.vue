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
import { useGameStore } from '../stores/game';
import Leaderboard from '../components/Leaderboard.vue';

const gameStore = useGameStore();
const currentUserId = computed(() => gameStore.userId ?? 0);
</script>

<style scoped>
.leaderboard-view {
  min-height: 100vh;
  background: #000000;
  padding: 20px;
}
</style>