<template>
  <div class="menu-view">
    <MainMenu 
      :isAdmin="isAdmin"
      :userName="userName"
      :userStats="userStats"
      :dailyChallenge="dailyChallenge"
      @start-game="startGame"
      @show-leaderboard="showLeaderboard"
      @show-stats="showStats"
      @show-admin="showAdmin"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';  // ← добавили computed
import { useRouter } from 'vue-router';
import { useGameStore } from '../stores/game';
import MainMenu from '../components/MainMenu.vue';

const router = useRouter();
const gameStore = useGameStore();

const demoDailyChallenge = {
  text: 'Правильно угадай 10 коллег подряд',
  reward: 50
};

const userName = ref('');
const isAdmin = ref(false);
const dailyChallenge = ref(demoDailyChallenge);

// Исправление: используем computed с gameStore
const userStats = computed(() => ({
  totalScore: gameStore.score,
  accuracy: gameStore.accuracy,
  bestStreak: gameStore.bestStreak
}));

const startGame = () => {
  router.push('/game');
};

const showLeaderboard = () => {
  router.push('/leaderboard');
};

const showStats = () => {
  router.push('/stats');
};

const showAdmin = () => {
  router.push('/admin');
};

onMounted(() => {
  const telegram = (window as any).Telegram?.WebApp;
  if (telegram) {
    telegram.ready();
    telegram.expand();
    
    const initDataUnsafe = telegram.initDataUnsafe;
    if (initDataUnsafe && initDataUnsafe.user) {
      userName.value = `${initDataUnsafe.user.first_name || ''} ${initDataUnsafe.user.last_name || ''}`.trim();
      isAdmin.value = initDataUnsafe.user.username === 'admin';
    }
  } else {
    userName.value = 'Тестовый Пользователь';
    isAdmin.value = true;
  }
});
</script>

<style scoped>
.menu-view {
  min-height: 100vh;
  background: #000000;
  padding: 20px;
}
</style>