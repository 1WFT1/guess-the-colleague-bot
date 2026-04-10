<template>
  <div class="admin-panel">
    <div class="admin-header">
      <h2>⚙️ Админ-панель</h2>
      <button @click="$emit('close')" class="close-btn">✕</button>
    </div>
    
    <div class="admin-content">
      <!-- Управление сотрудниками -->
      <div class="section">
        <div class="section-header">
          <h3>Управление сотрудниками</h3>
          <div class="actions">            
            <label class="btn btn-secondary file-label">
              📁 Загрузить CSV
              <input type="file" accept=".csv" style="display: none" @change="handleFileUpload" />
            </label>
            <button @click="openAddModal" class="btn btn-primary">➕ Добавить сотрудника</button>
            <button @click="exportData" class="btn btn-secondary">📤 Экспорт</button>
          </div>
        </div>

        <!-- Поиск под кнопками -->
        <div class="search-container">
          <div class="search-box">
            <span class="search-icon">🔍</span>
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="Поиск по ФИО или отделу..."
              class="search-input"
            />
            <button v-if="searchQuery" @click="clearSearch" class="search-clear">✕</button>
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
              <tr v-if="filteredEmployees.length === 0">
                <td colspan="6" class="empty-row">👨‍💼 Сотрудники не найдены</td>
              </tr>
              <tr v-for="employee in paginatedEmployees" :key="employee.id">
                <td class="id-cell" :title="String(employee.id)">
                  {{ formatId(employee.id) }}
                </td>
                <td class="name-cell">{{ employee.fullName }}</td>
                <td class="dept-cell">{{ employee.department || '—' }}</td>
                <td class="photo-cell">
                  <span v-if="employee.photoUrl" class="status-icon success">☑️</span>
                  <span v-else class="status-icon error">❌</span>
                </td>
                <td class="active-cell">
                  <label class="toggle-switch">
                    <input type="checkbox" :checked="employee.active" @change="toggleActive(employee.id)" />
                    <span class="toggle-slider"></span>
                  </label>
                </td>
                <td class="actions-cell">
                  <button @click="editEmployee(employee)" class="edit-btn" title="Редактировать">✏️</button>
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
        <div class="pagination-info">
          Показано {{ paginatedEmployees.length }} из {{ filteredEmployees.length }} сотрудников
        </div>
      </div>
      
      <!-- Статистика игры -->
      <div class="section">
        <div class="section-header">
          <h3>Статистика игры</h3>
          <button @click="refreshStats" class="btn btn-secondary">🔄 Обновить</button>
        </div>
        
        <div v-if="adminStats.isLoading.value" class="loading">Загрузка статистики...</div>
        <div v-else class="stats-grid">
          <div class="stat-card"><div class="stat-value">{{ adminStats.stats.value.totalPlayers }}</div><div class="stat-label">Всего игроков</div></div>
          <div class="stat-card"><div class="stat-value">{{ adminStats.stats.value.activeToday }}</div><div class="stat-label">Активных сегодня</div></div>
          <div class="stat-card"><div class="stat-value">{{ adminStats.stats.value.totalQuestions }}</div><div class="stat-label">Всего вопросов</div></div>
          <div class="stat-card"><div class="stat-value">{{ adminStats.stats.value.averageScore }}</div><div class="stat-label">Средний балл</div></div>
          <div class="stat-card"><div class="stat-value">{{ adminStats.stats.value.totalGames }}</div><div class="stat-label">Всего игр</div></div>
          <div v-if="adminStats.stats.value.topPlayer" class="stat-card"><div class="stat-value">{{ adminStats.stats.value.topPlayer.name }}</div><div class="stat-label">Лучший игрок ({{ adminStats.stats.value.topPlayer.score }} очков)</div></div>
        </div>
      </div>
    </div>

    <!-- Модальное окно (остается без изменений) -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ isEditing ? 'Редактирование сотрудника' : 'Добавление сотрудника' }}</h3>
          <button @click="closeModal" class="modal-close">✕</button>
        </div>
        
        <div class="modal-body">
          <div class="form-group">
            <label>ФИО *</label>
            <input v-model="formData.fullName" type="text" placeholder="Введите ФИО сотрудника" class="form-input" />
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
            <input v-model="formData.photoUrl" type="text" placeholder="https://example.com/photo.jpg" class="form-input" />
          </div>
          
          <div class="form-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="formData.active" />
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
          <button @click="saveEmployee" class="btn-save">{{ isEditing ? 'Сохранить' : 'Добавить' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useEmployeesApi } from '../../composables/useEmployeesApi';
import { useAdminStats } from '../../composables/useAdminStats';
import type { Employee, EmployeeForm } from '../../types/game';

const emit = defineEmits<{ close: [] }>();

const employeesApi = useEmployeesApi();
const adminStats = useAdminStats();

const employees = ref<Employee[]>([]);
const currentPage = ref(1);
const itemsPerPage = 5;
const showModal = ref(false);
const isEditing = ref(false);
const editingId = ref<number | null>(null);
const isLoading = ref(false);
const searchQuery = ref('');

const formData = ref<EmployeeForm>({
  fullName: '',
  department: '',
  photoUrl: '',
  active: true
});

// Фильтрация сотрудников по поиску
const filteredEmployees = computed(() => {
  if (!searchQuery.value.trim()) {
    return sortedEmployees.value;
  }
  const query = searchQuery.value.toLowerCase().trim();
  return sortedEmployees.value.filter(emp => 
    emp.fullName.toLowerCase().includes(query) ||
    (emp.department && emp.department.toLowerCase().includes(query))
  );
});

const sortedEmployees = computed(() => {
  return [...employees.value].sort((a, b) => {
    if (a.active === b.active) return 0;
    return a.active ? -1 : 1;
  });
});

const paginatedEmployees = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredEmployees.value.slice(start, end);
});

const totalPages = computed(() => Math.ceil(filteredEmployees.value.length / itemsPerPage));

const clearSearch = () => {
  searchQuery.value = '';
  currentPage.value = 1;
};

const formatId = (id: number): string => {
  return String(id).substring(0, 8) + '...';
};

const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--;
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};

// Загрузка CSV файла
const handleFileUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;
  if (!file.name.endsWith('.csv')) {
    alert('Пожалуйста, выберите CSV файл');
    return;
  }
  
  isLoading.value = true;
  try {
    const formData = new FormData();
    formData.append('file', file);
    const response = await fetch('http://localhost:8080/api/employees/upload-csv', {
      method: 'POST',
      body: formData
    });
    if (response.ok) {
      alert('Сотрудники успешно загружены');
      await loadEmployees();
    } else {
      const error = await response.text();
      alert('Ошибка загрузки: ' + error);
    }
  } catch (error) {
    console.error('Upload error:', error);
    alert('Ошибка при загрузке файла');
  } finally {
    isLoading.value = false;
    input.value = '';
  }
};

// Экспорт данных
const exportData = () => {
  const headers = ['ФИО', 'Отдел', 'Фото URL'];
  const rows = filteredEmployees.value.map(emp => [
    emp.fullName,
    emp.department,
    emp.photoUrl
  ]);
  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.map(cell => `"${cell || ''}"`).join(','))
  ].join('\n');
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `employees_${new Date().toISOString().split('T')[0]}.csv`;
  a.click();
  URL.revokeObjectURL(url);
  alert('Данные экспортированы');
};

const loadEmployees = async () => {
  employees.value = await employeesApi.getAll();
  adminStats.updateFromEmployees(employees.value);
};

const refreshStats = () => {
  adminStats.refresh();
};

const openAddModal = () => {
  isEditing.value = false;
  editingId.value = null;
  formData.value = {
    fullName: '',
    department: '',
    photoUrl: '',
    active: true
  };
  showModal.value = true;
};

const editEmployee = (employee: Employee) => {
  isEditing.value = true;
  editingId.value = employee.id;
  formData.value = {
    fullName: employee.fullName,
    department: employee.department,
    photoUrl: employee.photoUrl,
    active: employee.active
  };
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  isEditing.value = false;
  editingId.value = null;
};

const saveEmployee = async () => {
  if (!formData.value.fullName.trim()) {
    alert('Введите ФИО сотрудника');
    return;
  }
  
  const payload = {
    fullName: formData.value.fullName,
    department: formData.value.department || '',
    photoUrl: formData.value.photoUrl || '',
    active: formData.value.active === true
  };
  
  try {
    if (isEditing.value && editingId.value !== null) {
      await employeesApi.update(editingId.value, payload);
      alert('Сотрудник обновлен');
    } else {
      await employeesApi.create(payload);
      alert('Сотрудник добавлен');
    }
    await loadEmployees();
    closeModal();
  } catch (error: any) {
    console.error('Ошибка сохранения:', error);
    alert('Ошибка при сохранении');
  }
};

const deleteEmployee = async (id: number) => {
  if (confirm('Вы уверены, что хотите удалить этого сотрудника?')) {
    try {
      await employeesApi.delete(id);
      await loadEmployees();
      alert('Сотрудник удален');
    } catch (error) {
      console.error('Ошибка удаления:', error);
      alert('Ошибка при удалении');
    }
  }
};

const toggleActive = async (id: number) => {
  const employee = employees.value.find(e => e.id === id);
  if (employee) {
    try {
      const newStatus = !employee.active;
      await employeesApi.toggleActive(id, newStatus);
      employee.active = newStatus;
      await loadEmployees();
    } catch (error) {
      console.error('Ошибка изменения статуса:', error);
      alert('Ошибка при изменении статуса');
    }
  }
};

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"%3E%3Crect width="100" height="100" fill="%232a2a2a"/%3E%3Ctext x="50" y="55" text-anchor="middle" fill="%234f4ff4" font-size="40"%3E📷%3C/text%3E%3C/svg%3E';
};

onMounted(() => {
  loadEmployees();
});
</script>

<style scoped>
/* Стили для поиска под кнопками */
.search-container {
  margin-bottom: 20px;
}

.search-box {
  position: relative;
  display: inline-block;
  width: 100%;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 14px;
  color: #888;
}

.search-input {
  width: 100%;
  padding: 10px 30px 10px 35px;
  background: #2a2a2a;
  border: 1px solid #3a3a3a;
  border-radius: 10px;
  color: white;
  font-size: 14px;
  transition: all 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #4f4ff4;
  background: #1a1a1a;
}

.search-input::placeholder {
  color: #666;
}

.search-clear {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
  font-size: 14px;
  padding: 0 4px;
}

.search-clear:hover {
  color: white;
}

.empty-row {
  text-align: center;
  padding: 40px !important;
  color: #888;
}

.pagination-info {
  text-align: center;
  margin-top: 10px;
  font-size: 12px;
  color: #666;
}

/* Остальные стили */
.file-label {
  cursor: pointer;
  display: inline-block;
}

.file-label:hover {
  opacity: 0.9;
}

/* Остальные стили остаются без изменений */
.file-label {
  cursor: pointer;
  display: inline-block;
}

.file-label:hover {
  opacity: 0.9;
}
/* Добавьте стиль для кнопки-файла */
.file-label {
  cursor: pointer;
  display: inline-block;
}

.file-label:hover {
  opacity: 0.9;
}
</style>

<style scoped>
/* Стили остаются те же, что и в оригинале */
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
  from { transform: translateY(50px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  background: linear-gradient(135deg, #4f4ff4 0%, #6c6cff 100%);
  color: white;
}

.admin-header h2 { margin: 0; font-size: 20px; }

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: white;
  transition: transform 0.3s;
}

.close-btn:hover { transform: rotate(90deg); }

.admin-content { padding: 25px 30px; }

.section { margin-bottom: 35px; }

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

.actions { display: flex; gap: 12px; }

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
}

.employees-table td { color: #c0c0c0; font-size: 14px; }
.employees-table tr:hover td { background: #252525; }

.id-cell {
  font-weight: 500;
  color: #4f4ff4;
  width: 100px;
}

.name-cell { width: 150px; }
.photo-cell { width: 60px; text-align: center; }
.active-cell { width: 80px; text-align: center; }

.status-icon.success { color: #4caf50; }
.status-icon.error { color: #f44336; }

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

input:checked + .toggle-slider { background-color: #4f4ff4; }
input:checked + .toggle-slider:before { transform: translateX(22px); }

.actions-cell {
  width: 90px;
  text-align: center;
  white-space: nowrap;
}

.edit-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 6px;
  transition: all 0.2s;
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.edit-btn { color: #4caf50; }
.edit-btn:hover { background: rgba(76, 175, 80, 0.2); transform: scale(1.1); }

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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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

.loading {
  text-align: center;
  padding: 40px;
  color: #888;
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
  from { transform: translateY(30px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
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

.modal-header h3 { margin: 0; font-size: 18px; }

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: white;
  transition: transform 0.2s;
}

.modal-close:hover { transform: rotate(90deg); }

.modal-body { padding: 20px; }

.form-group { margin-bottom: 20px; }

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
  gap: 12px;
  cursor: pointer;
}

.checkbox-label input {
  width: 18px;
  height: 18px;
  cursor: pointer;
  margin: 0;
  vertical-align: middle;
  margin-right: 10px;
}

.checkbox-label span {
  color: #e0e0e0;
  font-size: 14px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  height: 18px;
  margin-top: 4px;
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

.btn-cancel:hover { background: #3a3a3a; }

.btn-save {
  background: #4f4ff4;
  color: white;
}

.btn-save:hover {
  background: #6c6cff;
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .admin-content { padding: 20px; }
  .section-header { flex-direction: column; align-items: stretch; }
  .actions { justify-content: center; }
  .stats-grid { grid-template-columns: repeat(2, 1fr); gap: 15px; }
  
}

@media (max-width: 600px) {
  .stats-grid { grid-template-columns: 1fr; }
}
</style>