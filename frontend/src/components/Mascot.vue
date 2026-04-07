<template>
  <div class="mascot">
    <div class="mascot-image-container">
      <img 
        :src="currentImage" 
        :alt="mood"
        class="mascot-image"
        :class="{ animated: isAnimated }"
        @error="handleImageError"
      />
    </div>
    <div v-if="message" class="mascot-message">{{ message }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const getImagePath = (imageName: string) => {
  return `/assets/images/${imageName}`;
};

const props = defineProps<{
  mood?: 'happy' | 'sad' | 'thinking' | 'neutral';
  message?: string;
  animated?: boolean;
}>();

const currentImage = computed(() => {
  switch (props.mood) {
    case 'happy': return getImagePath('codic-happy.png');
    case 'sad': return getImagePath('codic-sad.png');
    case 'thinking': return getImagePath('codic-thinking.png');
    default: return getImagePath('codic-shows.png');
  }
});

const isAnimated = computed(() => {
  return props.animated !== false;
});

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  console.warn(`Failed to load image: ${img.src}`);
  img.style.display = 'none';
  const parent = img.parentElement;
  if (parent) {
    const fallback = document.createElement('div');
    fallback.className = 'mascot-fallback';
    fallback.textContent = getFallbackEmoji();
    fallback.style.fontSize = '60px';
    parent.appendChild(fallback);
  }
};

const getFallbackEmoji = () => {
  switch (props.mood) {
    case 'happy': return '😊';
    case 'sad': return '😢';
    case 'thinking': return '🤔';
    default: return '🐱';
  }
};
</script>

<style scoped>
/* Стили остаются без изменений */
.mascot {
  text-align: center;
  margin-top: 10px;
}

.mascot-image-container {
  display: inline-block;
  position: relative;
}

.mascot-image {
  width: 150px;
  height: 150px;
  object-fit: contain;
  filter: drop-shadow(0 5px 15px rgba(0,0,0,0.2));
  transition: transform 0.3s;
}

.mascot-image:hover {
  transform: scale(1.05);
}

.mascot-image.animated {
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.mascot-message {
  margin-top: 10px;
  font-size: 14px;
  color: #888;
  font-style: italic;
}

.mascot-fallback {
  font-size: 60px;
  animation: bounce 0.5s ease;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}
</style>