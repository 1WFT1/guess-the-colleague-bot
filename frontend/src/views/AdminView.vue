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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}
</style>