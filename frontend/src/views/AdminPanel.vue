<template>
  <div class="admin-panel">
    <div class="admin-header">
      <h2>⚙️ Админ-панель Угадай коллегу</h2>
      <button @click="$emit('close')" class="close-btn">✕</button>
    </div>
    
    <div class="admin-content">
      <div class="admin-actions">
        <button class="action-btn">📁 Загрузить CSV</button>
        <button class="action-btn primary">➕ Добавить сотрудника</button>
        <button class="action-btn">📤 Экспорт</button>
      </div>
      
      <div class="employees-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>ФИО</th>
              <th>Отдел</th>
              <th>Фото</th>
              <th>Актив</th>
              <th>Действия</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="employee in employees" :key="employee.id">
              <td>{{ employee.id }}</td>
              <td>{{ employee.fullName }}</td>
              <td>{{ employee.department }}</td>
              <td>
                <span v-if="employee.photoUrl">☑️</span>
                <span v-else>❌</span>
              </td>
              <td>
                <input type="checkbox" :checked="employee.isActive" disabled />
              </td>
              <td>
                <button class="edit-btn" @click="editEmployee(employee)">✏️</button>
                <button class="delete-btn" @click="deleteEmployee(employee.id)">🗑️</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div class="pagination">
        <button class="page-btn" :disabled="currentPage === 1">‹</button>
        <span class="page-info">Страница {{ currentPage }} из {{ totalPages }}</span>
        <button class="page-btn" :disabled="currentPage === totalPages">›</button>
      </div>
      
      <div class="game-stats">
        <h3>Статистика игры</h3>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-value">{{ gameStats.totalPlayers }}</div>
            <div class="stat-label">Всего игроков</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ gameStats.activeToday }}</div>
            <div class="stat-label">Активных сегодня</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ gameStats.totalQuestions }}</div>
            <div class="stat-label">Всего вопросов</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ gameStats.averageScore }}</div>
            <div class="stat-label">Средний балл</div>
          </div>
        </div>
        <button class="detail-btn">☑️ Детальная статистика</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const emit = defineEmits<{
  close: [];
}>();

const employees = ref([
  { id: 1, fullName: 'Анна Иванова', department: 'Разработка', photoUrl: 'https://...', isActive: true },
  { id: 2, fullName: 'Петр Сидоров', department: 'Маркетинг', photoUrl: 'https://...', isActive: true },
  { id: 3, fullName: 'Елена Козлова', department: 'HR', photoUrl: 'https://...', isActive: true },
]);

const currentPage = ref(1);
const totalPages = ref(26);

const gameStats = ref({
  totalPlayers: 127,
  activeToday: 45,
  totalQuestions: 3452,
  averageScore: 234
});

const editEmployee = (employee: any) => {
  console.log('Edit employee:', employee);
  alert(`Редактирование сотрудника: ${employee.fullName}`);
};

const deleteEmployee = (id: number) => {
  if (confirm('Вы уверены, что хотите удалить этого сотрудника?')) {
    console.log('Delete employee:', id);
    alert('Сотрудник удален');
  }
};
</script>

<style scoped>
.admin-panel {
  background: white;
  border-radius: 20px;
  max-width: 1200px;
  margin: 0 auto;
  overflow: hidden;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateY(-50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.admin-header h2 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: white;
}

.admin-content {
  padding: 20px;
}

.admin-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 10px 20px;
  background: #f0f0f0;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.action-btn.primary {
  background: #667eea;
  color: white;
}

.employees-table {
  overflow-x: auto;
  margin-bottom: 20px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

th {
  background: #f8f9fa;
  font-weight: bold;
}

.edit-btn, .delete-btn {
  padding: 5px 10px;
  margin: 0 5px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.edit-btn {
  background: #4caf50;
  color: white;
}

.delete-btn {
  background: #f44336;
  color: white;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-bottom: 30px;
}

.page-btn {
  padding: 5px 10px;
  background: #f0f0f0;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.game-stats {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
}

.game-stats h3 {
  margin-bottom: 15px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
}

.stat-label {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

.detail-btn {
  width: 100%;
  padding: 10px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
</style>