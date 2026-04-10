<template>
  <div class="mascot">
    <div class="mascot-image-container">
      <img 
        :src="currentImage" 
        :alt="mood"
        class="mascot-image"
        :style="{ opacity: isLoaded ? 1 : 1 }"
      />
    </div>
    <div v-if="message" class="mascot-message">{{ message }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';

const props = defineProps<{
  mood?: 'happy' | 'sad' | 'thinking' | 'neutral';
  message?: string;
}>();

const images = ref<Record<string, HTMLImageElement>>({});
const isLoaded = ref(true);

const getImagePath = (imageName: string) => {
  return `/assets/images/${imageName}`;
};

const currentImage = computed(() => {
  switch (props.mood) {
    case 'happy': return getImagePath('codic-happy.png');
    case 'sad': return getImagePath('codic-sad.png');
    case 'thinking': return getImagePath('codic-thinking.png');
    default: return getImagePath('codic-shows.png');
  }
});

// Предзагрузка всех изображений при монтировании
onMounted(() => {
  const imageNames = ['codic-happy.png', 'codic-sad.png', 'codic-thinking.png', 'codic-shows.png'];
  imageNames.forEach(name => {
    const img = new Image();
    img.src = getImagePath(name);
    images.value[name] = img;
  });
});
</script>

<style scoped>
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
  transition: none;
}

.mascot-message {
  margin-top: 10px;
  font-size: 14px;
  color: #888;
  font-style: italic;
}
</style>