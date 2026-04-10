<template>
  <div id="app">
    <MenuView />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import MenuView from './views/MenuView.vue';

onMounted(() => {
  // Настройка Telegram WebApp
  const tg = (window as any).Telegram?.WebApp;
  if (tg) {
    // Устанавливаем цвета темы (используем цвета вашего приложения)
    tg.setBackgroundColor('#1a1a1a');
    tg.setHeaderColor('#1a1a1a');
    tg.setBottomBarColor('#1a1a1a');
    
    // Расширяем на весь экран
    tg.expand();
    
    // Сообщаем, что приложение готово
    tg.ready();
    
    console.log('Telegram WebApp initialized with theme:', tg.themeParams);
  }
});
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
  background: #1a1a1a;  /* Изменено с #000000 на #1a1a1a */
}

#app {
  min-height: 100vh;
  background: #1a1a1a;  /* Изменено с #000000 на #1a1a1a */
}
</style>