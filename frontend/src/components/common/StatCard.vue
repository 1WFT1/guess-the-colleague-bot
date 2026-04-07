<!-- components/common/StatCard.vue -->
<template>
  <div class="stat-card" :style="{ borderColor: color }">
    <div class="stat-value" :style="{ color: color }">
      {{ value }}{{ suffix }}
    </div>
    <div class="stat-label">{{ label }}</div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  value: number | string;
  label: string;
  color?: string;
  suffix?: string;
}>();
</script>

<style scoped>
.stat-card {
  background: #2a2a2a;
  padding: 15px;
  border-radius: 15px;
  text-align: center;
  transition: transform 0.3s;
  border: 1px solid #3a3a3a;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #4f4ff4;
}

.stat-label {
  font-size: 12px;
  color: #888;
  margin-top: 5px;
}
</style>