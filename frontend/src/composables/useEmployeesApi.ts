/**
 * Хук для работы с API сотрудников
 * Предоставляет методы для CRUD операций и управления статусом активности
 */

import { ref } from 'vue';
import gameApi from '../api/game';
import type { Employee } from '../types/game';

export function useEmployeesApi() {
  const isLoading = ref(false);  // Флаг загрузки
  const error = ref<string | null>(null);  // Сообщение об ошибке

  /**
   * Получить всех сотрудников (для админ-панели)
   */
  const getAll = async (): Promise<Employee[]> => {
    const response = await gameApi.getEmployees();  // Возвращает всех сотрудников
    return response;
  };

  /**
   * Создать нового сотрудника
   * @param data Данные сотрудника (fullName, department, photoUrl, active)
   */
  const create = async (data: any): Promise<Employee | null> => {
    isLoading.value = true;
    try {
      console.log('📤 useEmployeesApi.create received:', data);
      const result = await gameApi.createEmployee(data);
      console.log('✅ useEmployeesApi.create success:', result);
      return result;
    } catch (err: any) {
      console.error('❌ useEmployeesApi.create error:', err);
      console.error('❌ err.response:', err.response);
      console.error('❌ err.response.data:', err.response?.data);
      error.value = 'Ошибка создания сотрудника';
      return null;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * Обновить данные сотрудника
   * @param id ID сотрудника
   * @param data Новые данные (fullName, department, photoUrl, active)
   */
  const update = async (id: number, data: any): Promise<Employee | null> => {
    isLoading.value = true;
    try {
      const payload = {
        fullName: data.fullName || '',
        department: data.department || '',
        photoUrl: data.photoUrl || '',
        active: data.active === true
      };
      const result = await gameApi.updateEmployee(id, payload);
      return result;
    } catch (err) {
      console.error('Error updating employee:', err);
      error.value = 'Ошибка обновления сотрудника';
      return null;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * Удалить сотрудника (мягкое удаление, isActive = false)
   * @param id ID сотрудника
   */
  const deleteEmployee = async (id: number): Promise<boolean> => {
    isLoading.value = true;
    try {
      await gameApi.deleteEmployee(id);
      return true;
    } catch (err) {
      error.value = 'Ошибка удаления сотрудника';
      return false;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * Переключить статус активности сотрудника
   * @param id ID сотрудника
   * @param active Новый статус (true/false)
   */
  const toggleActive = async (id: number, active: boolean): Promise<boolean> => {
    isLoading.value = true;
    try {
      await gameApi.toggleEmployeeActive(id, active);
      return true;
    } catch (err) {
      error.value = 'Ошибка изменения статуса';
      return false;
    } finally {
      isLoading.value = false;
    }
  };

  return {
    isLoading,
    error,
    getAll,
    create,
    update,
    delete: deleteEmployee,
    toggleActive
  };
}