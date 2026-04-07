// composables/useEmployeesApi.ts
import { ref } from 'vue';
import gameApi from '../api/game';
import type { Employee } from '../types/game';

export function useEmployeesApi() {
  const isLoading = ref(false);
  const error = ref<string | null>(null);

  const getAll = async (): Promise<Employee[]> => {
    isLoading.value = true;
    error.value = null;
    try {
      const employees = await gameApi.getEmployees();
      return employees.map((emp: any) => ({
        id: emp.id,
        fullName: emp.fullName,
        department: emp.department,
        photoUrl: emp.photoUrl,
        isActive: emp.active,
        createdAt: emp.createdAt,
        updatedAt: emp.updatedAt
      }));
    } catch (err) {
      error.value = 'Ошибка загрузки сотрудников';
      console.error(err);
      return [];
    } finally {
      isLoading.value = false;
    }
  };

  const create = async (data: Partial<Employee>): Promise<Employee | null> => {
    isLoading.value = true;
    try {
      const result = await gameApi.createEmployee(data);
      return result;
    } catch (err) {
      error.value = 'Ошибка создания сотрудника';
      return null;
    } finally {
      isLoading.value = false;
    }
  };

  const update = async (id: number, data: Partial<Employee>): Promise<Employee | null> => {
    isLoading.value = true;
    try {
      const result = await gameApi.updateEmployee(id, data);
      return result;
    } catch (err) {
      error.value = 'Ошибка обновления сотрудника';
      return null;
    } finally {
      isLoading.value = false;
    }
  };

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

  const toggleActive = async (id: number, isActive: boolean): Promise<boolean> => {
    isLoading.value = true;
    try {
      await gameApi.toggleEmployeeActive(id, isActive);
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