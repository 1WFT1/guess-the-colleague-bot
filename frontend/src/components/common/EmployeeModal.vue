<!-- components/common/EmployeeModal.vue -->
<template>
  <div v-if="visible" class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <div class="modal-header">
        <h3>{{ isEditing ? 'Редактирование сотрудника' : 'Добавление сотрудника' }}</h3>
        <button @click="close" class="modal-close">✕</button>
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
            <option v-for="dept in departments" :key="dept" :value="dept">
              {{ dept }}
            </option>
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
          <div></div>
          <label class="checkbox-label">
            <input type="checkbox" v-model="formData.active" />
            <span>Активен1</span>
          </label>
        </div>
        
        <div v-if="formData.photoUrl" class="photo-preview">
          <p>Предпросмотр:</p>
          <img :src="formData.photoUrl" alt="Фото сотрудника" @error="handleImageError" />
        </div>
      </div>
      
      <div class="modal-footer">
        <button @click="close" class="btn-cancel">Отмена</button>
        <button @click="save" class="btn-save">
          {{ isEditing ? 'Сохранить' : 'Добавить' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import type { Employee } from '../../types/game';

const props = defineProps<{
  visible: boolean;
  isEditing: boolean;
  employee: Employee | null;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  save: [data: Partial<Employee>];
}>();

const departments = ['Разработка', 'Маркетинг', 'HR', 'Аналитика', 'Дизайн', 'Продажи', 'Поддержка'];

const formData = ref({
  fullName: '',
  department: '',
  photoUrl: '',
  active: true
});

watch(() => props.employee, (employee) => {
  if (employee) {
    formData.value = {
      fullName: employee.fullName,
      department: employee.department,
      photoUrl: employee.photoUrl,
      active: employee.active
    };
  } else {
    formData.value = {
      fullName: '',
      department: '',
      photoUrl: '',
      active: true
    };
  }
}, { immediate: true });

const close = () => {
  emit('update:visible', false);
};

const save = () => {
  if (!formData.value.fullName.trim()) {
    alert('Введите ФИО сотрудника');
    return;
  }
  emit('save', formData.value);
  close();
};

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"%3E%3Crect width="100" height="100" fill="%232a2a2a"/%3E%3Ctext x="50" y="55" text-anchor="middle" fill="%234f4ff4" font-size="40"%3E📷%3C/text%3E%3C/svg%3E';
};
</script>

<style scoped>
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
  gap: 10px;
  cursor: pointer;
}

.checkbox-label input {
  width: 18px;
  height: 18px;
  cursor: pointer;
  order: 1; /* Чекбокс справа */
}

.checkbox-label span {
  color: #e0e0e0;
  order: 0; /* Текст слева от чекбокса */
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
</style>