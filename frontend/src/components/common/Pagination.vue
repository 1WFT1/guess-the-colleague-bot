<!-- components/common/Pagination.vue -->
<template>
  <div class="pagination">
    <button 
      @click="prevPage" 
      :disabled="currentPage === 1" 
      class="page-btn"
    >
      ‹
    </button>
    <span class="page-info">
      Страница {{ currentPage }} из {{ totalPages }}
    </span>
    <button 
      @click="nextPage" 
      :disabled="currentPage === totalPages" 
      class="page-btn"
    >
      ›
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  currentPage: number;
  totalItems: number;
  itemsPerPage: number;
}>();

const emit = defineEmits<{
  'update:currentPage': [value: number];
}>();

const totalPages = computed(() => Math.ceil(props.totalItems / props.itemsPerPage));

const prevPage = () => {
  if (props.currentPage > 1) {
    emit('update:currentPage', props.currentPage - 1);
  }
};

const nextPage = () => {
  if (props.currentPage < totalPages.value) {
    emit('update:currentPage', props.currentPage + 1);
  }
};
</script>

<style scoped>
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
}

.page-btn {
  padding: 8px 16px;
  background: #2a2a2a;
  border: 1px solid #3a3a3a;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  color: #e0e0e0;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #4f4ff4;
  border-color: #4f4ff4;
  color: white;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  color: #888;
  font-size: 14px;
}
</style>