<!-- components/ErrorLogger.vue -->
<template>
  <div v-if="errors.length > 0" class="error-logger">
    <div class="error-header" @click="isMinimized = !isMinimized">
      <span>⚠️ Ошибки ({{ errors.length }})</span>
      <button class="close-btn" @click.stop="clearErrors">✕</button>
    </div>
    <div v-if="!isMinimized" class="error-list">
      <div v-for="(error, index) in errors" :key="index" class="error-item">
        <span class="error-time">{{ error.time }}</span>
        <span class="error-message">{{ error.message }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

interface ErrorItem {
  time: string;
  message: string;
}

const errors = ref<ErrorItem[]>([]);
const isMinimized = ref(false);

// Добавить ошибку
const addError = (message: string) => {
  errors.value.push({
    time: new Date().toLocaleTimeString(),
    message: message
  });
  console.error('[UI Error]', message);
};

// Очистить ошибки
const clearErrors = () => {
  errors.value = [];
};

// Перехват глобальных ошибок
window.addEventListener('error', (event) => {
  addError(`JS Error: ${event.message}`);
});

window.addEventListener('unhandledrejection', (event) => {
  addError(`Promise Error: ${event.reason}`);
});

// Экспортируем функцию для добавления ошибок из других компонентов
(window as any).showError = addError;

// Также показываем ошибки fetch
const originalFetch = window.fetch;
window.fetch = async (...args) => {
  try {
    const response = await originalFetch(...args);
    if (!response.ok) {
      addError(`HTTP ${response.status}: ${args[0]}`);
    }
    return response;
  } catch (err: any) {
    addError(`Network Error: ${err.message}`);
    throw err;
  }
};
</script>

<style scoped>
.error-logger {
  position: fixed;
  bottom: 10px;
  left: 10px;
  right: 10px;
  z-index: 10000;
  background: rgba(0, 0, 0, 0.95);
  border-radius: 10px;
  border-left: 4px solid #f44336;
  font-family: monospace;
  font-size: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.5);
}

.error-header {
  padding: 8px 12px;
  background: #1a1a1a;
  color: #f44336;
  border-radius: 10px 10px 0 0;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.error-list {
  max-height: 200px;
  overflow-y: auto;
  padding: 5px;
}

.error-item {
  padding: 5px 10px;
  border-bottom: 1px solid #333;
  display: flex;
  gap: 10px;
  color: #ccc;
}

.error-time {
  color: #888;
  font-size: 10px;
  min-width: 60px;
}

.error-message {
  flex: 1;
  word-break: break-word;
}

.close-btn {
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
  font-size: 16px;
}

.close-btn:hover {
  color: white;
}
</style>