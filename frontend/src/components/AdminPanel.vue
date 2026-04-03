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
            <button @click="uploadCSV" class="btn btn-secondary">
              📁 Загрузить CSV
            </button>
            <button @click="openAddModal" class="btn btn-primary">
              ➕ Добавить сотрудника
            </button>
            <button @click="exportData" class="btn btn-secondary">
              📤 Экспорт
            </button>
          </div>
        </div>
        
        <div class="table-wrapper">
          <table class="employees-table">
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
            <tr v-for="employee in paginatedEmployees" :key="employee.id">
              <td class="id-cell" :title="String(employee.id)">
                {{ String(employee.id).substring(0, 8) }}...
              </td>
              <td class="name-cell">{{ employee.fullName }}</td>
              <td class="dept-cell">{{ employee.department }}</td>
              <td class="photo-cell">
                <span v-if="employee.photoUrl" class="status-icon success">☑️</span>
                <span v-else class="status-icon error">❌</span>
              </td>
              <td class="active-cell">
                <label class="toggle-switch">
                  <input 
                    type="checkbox" 
                    :checked="employee.isActive" 
                    @change="toggleActive(employee.id)"
                  />
                  <span class="toggle-slider"></span>
                </label>
              </td>
              <td class="actions-cell">
                <button @click="editEmployee(employee)" class="edit-btn" title="Редактировать">
                  ✏️
                </button>
                <button @click="deleteEmployee(employee.id)" class="delete-btn" title="Удалить">
                  🗑️
                </button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
        
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
      </div>
    </div>

    <!-- Модальное окно добавления/редактирования сотрудника -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ isEditing ? 'Редактирование сотрудника' : 'Добавление сотрудника' }}</h3>
          <button @click="closeModal" class="modal-close">✕</button>
        </div>
        
        <div class="modal-body">
          <div class="form-group">
            <label>ФИО *</label>
            <input 
              v-model="formData.fullName" 
              type="text" 
              placeholder="Введите ФИО сотрудника"
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label>Отдел</label>
            <select v-model="formData.department" class="form-select">
              <option value="">Выберите отдел</option>
              <option value="Разработка">Разработка</option>
              <option value="Маркетинг">Маркетинг</option>
              <option value="HR">HR</option>
              <option value="Аналитика">Аналитика</option>
              <option value="Дизайн">Дизайн</option>
              <option value="Продажи">Продажи</option>
              <option value="Поддержка">Поддержка</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>Фото (URL)</label>
            <input 
              v-model="formData.photoUrl" 
              type="text" 
              placeholder="https://example.com/photo.jpg"
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="formData.isActive" />
              <span>Активен</span>
            </label>
          </div>
          
          <div v-if="formData.photoUrl" class="photo-preview">
            <p>Предпросмотр:</p>
            <img :src="formData.photoUrl" alt="Фото сотрудника" @error="handleImageError" />
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeModal" class="btn-cancel">Отмена</button>
          <button @click="saveEmployee" class="btn-save">
            {{ isEditing ? 'Сохранить' : 'Добавить' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { useGameStore } from '../stores/game';

const API_URL = 'http://localhost:8080/api';

const emit = defineEmits<{
  close: [];
}>();

// Интерфейс сотрудника
interface Employee {
  id: number;
  fullName: string;
  department: string;
  photoUrl: string;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// Интерфейс формы
interface EmployeeForm {
  fullName: string;
  department: string;
  photoUrl: string;
  isActive: boolean;
}

// Начальные данные сотрудников
const employees = ref<Employee[]>([]);
const loading = ref(false);

// Реактивные данные
const currentPage = ref(1);
const itemsPerPage = 5;

// Статистика игры
const gameStats = ref({
  totalPlayers: employees.value.length,
  activeToday: employees.value.filter(e => e.isActive).length,
  totalQuestions: 3452,
  averageScore: 234
});

// Модальное окно
const showModal = ref(false);
const isEditing = ref(false);
const editingId = ref<number | null>(null);

// Форма
const formData = ref<EmployeeForm>({
  fullName: '',
  department: '',
  photoUrl: '',
  isActive: true
});

// Пагинация
const paginatedEmployees = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return employees.value.slice(start, end);
});

onMounted(() => {
  loadEmployees();
});

const loadEmployees = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/employees');
    // Преобразуем поле active с сервера в isActive для компонента
    employees.value = response.data.map((emp: any) => ({
      id: emp.id,
      fullName: emp.fullName,
      department: emp.department,
      photoUrl: emp.photoUrl,
      isActive: emp.active,  // active с сервера → isActive в компоненте
      createdAt: emp.createdAt,
      updatedAt: emp.updatedAt
    }));
    updateStats();
  } catch (error) {
    console.error('Ошибка загрузки сотрудников:', error);
  }
};
const totalPages = computed(() => {
  return Math.ceil(employees.value.length / itemsPerPage);
});

// Обновление статистики
const updateStats = () => {
  // Количество сотрудников
  gameStats.value.totalPlayers = employees.value.length;
  
  // Количество активных сегодня (из localStorage)
  const today = new Date().toISOString().split('T')[0];
  const todayStats = localStorage.getItem(`dailyStats_${today}`);
  if (todayStats) {
    try {
      const stats = JSON.parse(todayStats);
      gameStats.value.activeToday = stats.activeUsers || 0;
    } catch (e) {
      gameStats.value.activeToday = 0;
    }
  } else {
    gameStats.value.activeToday = 0;
  }
  
  // Всего вопросов из GameStore
  const gameStore = useGameStore();
  gameStats.value.totalQuestions = gameStore.totalQuestions;
  
  // Реальный средний балл из всех игроков
  let totalScore = 0;
  let playersCount = 0;
  
  // Собираем статистику всех игроков
  const allPlayersStats = localStorage.getItem('allPlayersStats');
  if (allPlayersStats) {
    try {
      const players = JSON.parse(allPlayersStats);
      players.forEach((player: any) => {
        totalScore += player.totalScore || 0;
        playersCount++;
      });
    } catch (e) {
      console.error('Failed to parse players stats', e);
    }
  }
  
  // Добавляем текущего игрока
  const currentStats = localStorage.getItem('gameStats');
  if (currentStats) {
    try {
      const stats = JSON.parse(currentStats);
      totalScore += stats.score || 0;
      playersCount++;
    } catch (e) {
      console.error('Failed to parse current stats', e);
    }
  }
  
  // Рассчитываем средний балл
  gameStats.value.averageScore = playersCount > 0 ? Math.round(totalScore / playersCount) : 0;
};

// Открыть модалку добавления
const openAddModal = () => {
  isEditing.value = false;
  editingId.value = null;
  formData.value = {
    fullName: '',
    department: '',
    photoUrl: '',
    isActive: true
  };
  showModal.value = true;
};

// Открыть модалку редактирования
const editEmployee = (employee: Employee) => {
  isEditing.value = true;
  editingId.value = employee.id;
  formData.value = {
    fullName: employee.fullName,
    department: employee.department,
    photoUrl: employee.photoUrl,
    isActive: employee.isActive
  };
  showModal.value = true;
};

// Закрыть модалку
const closeModal = () => {
  showModal.value = false;
  isEditing.value = false;
  editingId.value = null;
};

// Сохранить сотрудника
const saveEmployee = async () => {
  if (!formData.value.fullName.trim()) {
    alert('Введите ФИО сотрудника');
    return;
  }
  
  try {
    if (isEditing.value && editingId.value !== null) {
      // Редактирование
      await axios.put(`${API_URL}/employees/${editingId.value}`, {
        fullName: formData.value.fullName,
        department: formData.value.department,
        photoUrl: formData.value.photoUrl,
        isActive: formData.value.isActive
      });
      alert('Сотрудник обновлен');
    } else {
      // Добавление
      await axios.post(`${API_URL}/employees`, {
        fullName: formData.value.fullName,
        department: formData.value.department,
        photoUrl: formData.value.photoUrl,
        isActive: formData.value.isActive
      });
      alert('Сотрудник добавлен');
    }
    await loadEmployees(); // Перезагружаем список
    closeModal();
  } catch (error) {
    console.error('Ошибка сохранения:', error);
    alert('Ошибка при сохранении');
  }
};

// Удалить сотрудника
const deleteEmployee = async (id: number) => {
  if (confirm('Вы уверены, что хотите удалить этого сотрудника?')) {
    try {
      await axios.delete(`${API_URL}/employees/${id}`);
      await loadEmployees();
      alert('Сотрудник удален');
    } catch (error) {
      console.error('Ошибка удаления:', error);
      alert('Ошибка при удалении');
    }
  }
};

// Переключить статус активности
const toggleActive = async (id: number) => {
  const employee = employees.value.find(e => e.id === id);
  if (employee) {
    try {
      const newStatus = !employee.isActive;
      await axios.patch(`http://localhost:8080/api/employees/${id}/active`, {
        isActive: newStatus
      });
      employee.isActive = newStatus;
      updateStats();
    } catch (error) {
      console.error('Ошибка изменения статуса:', error);
      alert('Ошибка при изменении статуса');
    }
  }
};


// Обработка ошибки загрузки фото
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  // Используем data:image вместо внешнего URL
  img.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"%3E%3Crect width="100" height="100" fill="%232a2a2a"/%3E%3Ctext x="50" y="55" text-anchor="middle" fill="%234f4ff4" font-size="40"%3E📷%3C/text%3E%3C/svg%3E';
};

// Пагинация
const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--;
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};

// Загрузка CSV
const uploadCSV = () => {
  alert('Загрузка CSV файла');
};

// Экспорт данных
const exportData = () => {
  const data = JSON.stringify(employees.value, null, 2);
  const blob = new Blob([data], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `employees_${new Date().toISOString().split('T')[0]}.json`;
  a.click();
  URL.revokeObjectURL(url);
  alert('Данные экспортированы');
};

// В конце игры или при ответе на вопрос
const saveDailyActivity = () => {
  const today = new Date().toISOString().split('T')[0];
  const dailyStats = localStorage.getItem(`dailyStats_${today}`);
  
  let stats: any = { activeUsers: 0, totalGames: 0 };
  if (dailyStats) {
    stats = JSON.parse(dailyStats);
  }
  
  const userId = localStorage.getItem('currentUserId');
  if (userId && !stats[`user_${userId}`]) {
    stats.activeUsers++;
    stats[`user_${userId}`] = true;
  }
  
  stats.totalGames++;
  localStorage.setItem(`dailyStats_${today}`, JSON.stringify(stats));
};
</script>

<style scoped>
.admin-panel {
  background: #1a1a1a;
  border-radius: 30px;
  max-width: 1200px;
  margin: 0 auto;
  overflow: hidden;
  animation: slideIn 0.3s ease;
  border: 1px solid #2a2a2a;
}

@keyframes slideIn {
  from {
    transform: translateY(50px);
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
  padding: 20px 30px;
  background: linear-gradient(135deg, #4f4ff4 0%, #6c6cff 100%);
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
  padding: 25px 30px;
}

.section {
  margin-bottom: 35px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.section-header h3 {
  margin: 0;
  color: white;
  font-size: 18px;
}

.actions {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 8px 18px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-primary {
  background: #4f4ff4;
  color: white;
}

.btn-primary:hover {
  background: #6c6cff;
  transform: translateY(-2px);
}

.btn-secondary {
  background: #2a2a2a;
  color: #e0e0e0;
  border: 1px solid #3a3a3a;
}

.btn-secondary:hover {
  background: #3a3a3a;
  transform: translateY(-2px);
  border-color: #4f4ff4;
}

.table-wrapper {
  overflow-x: auto;
  margin-bottom: 20px;
  border-radius: 16px;
  border: 1px solid #2a2a2a;
}

.employees-table {
  width: 100%;
  border-collapse: collapse;
  background: #1a1a1a;
  table-layout: fixed;
}

.employees-table th,
.employees-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid #2a2a2a;
}

.employees-table th {
  background: #252525;
  color: #e0e0e0;
  font-weight: 600;
  font-size: 13px;
  letter-spacing: 0.5px;
}

.employees-table td {
  color: #c0c0c0;
  font-size: 14px;
}

.employees-table tr:hover td {
  background: #252525;
}

.id-cell {
  font-weight: 500;
  color: #4f4ff4;
  width: 100px
}

.name-cell {
  font-weight: 500;
  width: 150px
}

.photo-cell {
  width: 60px;
  text-align: center;
}

.active-cell {
  width: 80px;
  text-align: center;
}

.status-icon {
  font-size: 16px;
}

.status-icon.success {
  color: #4caf50;
}

.status-icon.error {
  color: #f44336;
}

/* Toggle switch */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 22px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #3a3a3a;
  transition: 0.3s;
  border-radius: 22px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 2px;
  bottom: 2px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
}

input:checked + .toggle-slider {
  background-color: #4f4ff4;
}

input:checked + .toggle-slider:before {
  transform: translateX(22px);
}

.actions-cell {
  width: 90px;
  text-align: center;
  white-space: nowrap;
}

.edit-btn, .delete-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 6px;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
}

.edit-btn {
  color: #4caf50;
}

.edit-btn:hover {
  background: rgba(76, 175, 80, 0.2);
  transform: scale(1.1);
}

.delete-btn {
  color: #f44336;
}

.delete-btn:hover {
  background: rgba(244, 67, 54, 0.2);
  transform: scale(1.1);
}

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

/* Статистика */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: #2a2a2a;
  padding: 20px;
  border-radius: 16px;
  text-align: center;
  transition: all 0.3s;
  border: 1px solid #3a3a3a;
}

.stat-card:hover {
  transform: translateY(-3px);
  border-color: #4f4ff4;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #4f4ff4;
}

.stat-label {
  font-size: 12px;
  color: #888;
  margin-top: 8px;
}

/* Модальное окно */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: #1a1a1a;
  border-radius: 20px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  border: 1px solid #2a2a2a;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #2a2a2a;
  background: linear-gradient(135deg, #4f4ff4 0%, #6c6cff 100%);
  border-radius: 20px 20px 0 0;
  color: white;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: white;
  transition: transform 0.2s;
}

.modal-close:hover {
  transform: rotate(90deg);
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #e0e0e0;
  font-size: 14px;
  font-weight: 500;
}

.form-input,
.form-select {
  width: 100%;
  padding: 12px;
  background: #2a2a2a;
  border: 1px solid #3a3a3a;
  border-radius: 10px;
  color: white;
  font-size: 14px;
  transition: all 0.2s;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #4f4ff4;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.checkbox-label input {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.checkbox-label span {
  color: #e0e0e0;
}

.photo-preview {
  margin-top: 15px;
  text-align: center;
  padding: 15px;
  background: #2a2a2a;
  border-radius: 10px;
}

.photo-preview p {
  margin-bottom: 10px;
  color: #888;
  font-size: 12px;
}

.photo-preview img {
  max-width: 100px;
  max-height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding: 20px;
  border-top: 1px solid #2a2a2a;
}

.btn-cancel,
.btn-save {
  padding: 10px 24px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-cancel {
  background: #2a2a2a;
  color: #e0e0e0;
  border: 1px solid #3a3a3a;
}

.btn-cancel:hover {
  background: #3a3a3a;
}

.btn-save {
  background: #4f4ff4;
  color: white;
}

.btn-save:hover {
  background: #6c6cff;
  transform: translateY(-2px);
}

/* Адаптивность */
@media (max-width: 768px) {
  .admin-content {
    padding: 20px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .actions {
    justify-content: center;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
  
  .employees-table th,
  .employees-table td {
    padding: 10px 12px;
    font-size: 12px;
  }
}

@media (max-width: 600px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>