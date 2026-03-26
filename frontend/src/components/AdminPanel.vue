<template>
  <div class="admin-panel">
    <div class="admin-header">
      <h2>⚙️ Админ-панель Угадай коллегу</h2>
      <button @click="$emit('close')" class="close-btn">✕</button>
    </div>
    
    <div class="admin-content">
      <!-- Управление сотрудниками -->
      <div class="section">
        <div class="section-header">
          <h3>Управление сотрудниками</h3>
          <div class="actions">
            <button @click="uploadCSV" class="btn btn-secondary">📁 Загрузить CSV</button>
            <button @click="addEmployee" class="btn btn-primary">➕ Добавить сотрудника</button>
            <button @click="exportData" class="btn btn-secondary">📤 Экспорт</button>
          </div>
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
                  <input type="checkbox" :checked="employee.isActive" @change="toggleActive(employee.id)" />
                </td>
                <td>
                  <button @click="editEmployee(employee)" class="edit-btn">✏️</button>
                  <button @click="deleteEmployee(employee.id)" class="delete-btn">🗑️</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <div class="pagination">
          <button @click="prevPage" :disabled="currentPage === 1" class="page-btn">‹</button>
          <span class="page-info">Страница {{ currentPage }} из {{ totalPages }}</span>
          <button @click="nextPage" :disabled="currentPage === totalPages" class="page-btn">›</button>
        </div>
      </div>
      
      <!-- Статистика игры -->
      <div class="section">
        <div class="section-header">
          <h3>Статистика игры</h3>
        </div>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-value">{{ gameStats.totalPlayers }}</div>
            <div class="stat-label">Всего игроков</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ gameStats.activeToday }}</div>
            <div class="stat-label">Активных сегодня</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ gameStats.totalQuestions }}</div>
            <div class="stat-label">Всего вопросов</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ gameStats.averageScore }}</div>
            <div class="stat-label">Средний балл</div>
          </div>
        </div>
        <button @click="showDetailedStats" class="btn btn-primary full-width">☑️ Детальная статистика</button>
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
  { id: 4, fullName: 'Игорь Ветров', department: 'Аналитика', photoUrl: 'https://...', isActive: true },
  { id: 5, fullName: 'Светлана Морозова', department: 'Дизайн', photoUrl: 'https://...', isActive: true },
]);

const currentPage = ref(1);
const totalPages = ref(26);

const gameStats = ref({
  totalPlayers: 127,
  activeToday: 45,
  totalQuestions: 3452,
  averageScore: 234
});

const uploadCSV = () => {
  alert('Загрузка CSV файла');
};

const addEmployee = () => {
  alert('Добавление нового сотрудника');
};

const exportData = () => {
  alert('Экспорт данных');
};

const editEmployee = (employee: any) => {
  alert(`Редактирование сотрудника: ${employee.fullName}`);
};

const deleteEmployee = (id: number) => {
  if (confirm('Вы уверены, что хотите удалить этого сотрудника?')) {
    alert('Сотрудник удален');
  }
};

const toggleActive = (id: number) => {
  alert(`Изменение статуса сотрудника ${id}`);
};

const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--;
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};

const showDetailedStats = () => {
  alert('Детальная статистика');
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
  font-size: 20px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: white;
  transition: transform 0.3s;
}

.close-btn:hover {
  transform: rotate(90deg);
}

.admin-content {
  padding: 20px;
}

.section {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.section-header h3 {
  margin: 0;
  color: #333;
}

.actions {
  display: flex;
  gap: 10px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-secondary {
  background: #f0f0f0;
  color: #333;
}

.full-width {
  width: 100%;
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
  color: #333;
}

.edit-btn, .delete-btn {
  padding: 5px 10px;
  margin: 0 5px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 12px;
  transition: transform 0.3s;
}

.edit-btn:hover, .delete-btn:hover {
  transform: scale(1.05);
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
  gap: 15px;
  margin-top: 20px;
}

.page-btn {
  padding: 5px 12px;
  background: #f0f0f0;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #667eea;
  color: white;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.stat-card {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
  text-align: center;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #667eea;
}

.stat-label {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}
</style>