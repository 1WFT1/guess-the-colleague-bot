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
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import MainMenu from '../components/MainMenu.vue';

const demoUserStats = {
  totalScore: 0,
  accuracy: 0,
  bestStreak: 0
};

const demoDailyChallenge = {
  text: 'Правильно угадай 10 коллег подряд',
  reward: 50
};
const router = useRouter();
const userName = ref('');
const isAdmin = ref(false);
const userStats = ref({
  totalScore: demoUserStats.totalScore,
  accuracy: demoUserStats.accuracy,
  bestStreak: demoUserStats.bestStreak
});
const dailyChallenge = ref(demoDailyChallenge);

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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}
</style>