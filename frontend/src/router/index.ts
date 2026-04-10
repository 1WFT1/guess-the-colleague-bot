import { createRouter, createWebHistory } from 'vue-router';
import GameView from '../views/MenuView.vue';
import MenuView from '../views/MenuView.vue';

const router = createRouter({
  history: createWebHistory('/'),
  routes: [
    {
      path: '/',
      name: 'menu',
      component: MenuView,
    },
    {
      path: '/game',
      name: 'game',
      component: GameView,
    },
    {
      path: '/leaderboard',
      name: 'leaderboard',
      component: () => import('../views/LeaderboardView.vue'),
    },
    {
      path: '/stats',
      name: 'stats',
      component: () => import('../views/StatsView.vue'),
    },
  ],
});

export default router;