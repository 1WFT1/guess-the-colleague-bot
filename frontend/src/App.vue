<template>
  <div id="app">
    <GameView />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';

onMounted(() => {
  const tg = (window as any).Telegram?.WebApp;
  if (tg) {
    // Получаем цвета темы Telegram
    const themeParams = tg.themeParams;
    
    // Устанавливаем цвет фона из темы Telegram
    if (themeParams.bg_color) {
      document.body.style.backgroundColor = themeParams.bg_color;
      document.querySelector('#app')?.setAttribute('style', `background-color: ${themeParams.bg_color}`);
    }
    
    // Или принудительно устанавливаем свои цвета
    tg.setBackgroundColor('#1a1a1a');
    tg.setHeaderColor('#1a1a1a');
    tg.setBottomBarColor('#1a1a1a');
    
    tg.expand();
    tg.ready();
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