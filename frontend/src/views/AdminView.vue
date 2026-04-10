<template>
  <div class="admin-view">
    <AdminPanel @close="goBack" />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import AdminPanel from '../components/admin/AdminPanel.vue';

const router = useRouter();
const goBack = () => router.push('/');
</script>

<style scoped>
.admin-view {
  min-height: 100vh;
  background: #000000;
  padding: 20px;
}
</style>