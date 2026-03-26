<template>
  <div class="mascot">
    <div class="mascot-emoji">{{ emoji }}</div>
    <div v-if="message" class="mascot-message">{{ message }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  mood?: 'happy' | 'sad' | 'thinking' | 'neutral';
  message?: string;
}>();

const emoji = computed(() => {
  switch (props.mood) {
    case 'happy': return '😊';
    case 'sad': return '😢';
    case 'thinking': return '🤔';
    default: return '🐱';
  }
});
</script>

<style scoped>
.mascot {
  text-align: center;
  margin-top: 20px;
}

.mascot-emoji {
  font-size: 60px;
}

.mascot-message {
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}
</style>