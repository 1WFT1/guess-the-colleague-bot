<!-- components/common/WeeklyChart.vue -->
<template>
  <div class="chart-bars">
    <div 
      v-for="(score, day) in weeklyStats" 
      :key="day" 
      class="chart-bar-container"
    >
      <div class="chart-bar-wrapper">
        <div 
          class="chart-bar" 
          :style="{ 
            height: `${getBarHeight(score)}%`,
            backgroundColor: getBarColor(score)
          }"
        >
          <span class="bar-value">{{ score }}</span>
        </div>
      </div>
      <div class="bar-label">{{ day }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  weeklyStats: Record<string, number>;
}>();

const maxScore = computed(() => {
  const scores = Object.values(props.weeklyStats);
  if (scores.length === 0) return 1;
  return Math.max(...scores, 1);
});

const getBarHeight = (score: number) => {
  if (score === 0) return 10;
  return (score / maxScore.value) * 100;
};

const getBarColor = (score: number) => {
  if (score === 0) return '#3a3a3a';
  if (score === maxScore.value) return '#4f4ff4';
  const intensity = score / maxScore.value;
  return `rgba(79, 79, 244, ${0.3 + intensity * 0.7})`;
};
</script>

<style scoped>
.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 250px;
  gap: 10px;
  padding: 10px 0;
}

.chart-bar-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.chart-bar-wrapper {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.chart-bar {
  width: 100%;
  max-width: 50px;
  min-width: 30px;
  border-radius: 8px 8px 0 0;
  transition: all 0.5s ease;
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 5px;
  cursor: pointer;
}

.chart-bar:hover {
  transform: scaleX(1.05);
  filter: brightness(1.1);
}

.bar-value {
  color: white;
  font-size: 11px;
  font-weight: bold;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
}

.bar-label {
  font-size: 12px;
  color: #888;
  font-weight: 500;
}
</style>