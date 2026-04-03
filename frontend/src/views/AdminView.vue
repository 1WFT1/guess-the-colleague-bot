<template>
  <div class="admin-view">
    <AdminPanel @close="$router.push('/')" />
  </div>
</template>

<script setup lang="ts">
import AdminPanel from '../components/AdminPanel.vue';
</script>

<style scoped>
.admin-view {
  min-height: 100vh;
  background: #000000;
  padding: 20px;
}
</style>