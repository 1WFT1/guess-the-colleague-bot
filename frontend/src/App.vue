<template>
  <div id="app">
    <router-view />
    <ErrorLogger />
  </div>
</template>

<script setup lang="ts">
import GameView from './views/GameView.vue';
import ErrorLogger from './components/ErrorLogger.vue';
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
  background: #000000;
}

#app {
  min-height: 100vh;
  background: #000000;
}
</style>