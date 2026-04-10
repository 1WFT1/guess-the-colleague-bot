/**
 * Хук для получения данных пользователя из Telegram WebApp
 * Поддерживает получение из Telegram API и из URL параметров (для тестирования)
 */

import { ref, computed, onMounted } from 'vue';

export function useTelegram() {
  const user = ref<any>(null);           // Данные пользователя
  const isAdmin = ref(false);            // Флаг администратора
  const isLoading = ref(true);           // Флаг загрузки
  const ready = ref(false);              // Флаг готовности данных

  /**
   * Получение пользователя из Telegram WebApp API
   */
  const getUserFromTelegramAPI = () => {
    try {
      const telegram = (window as any).Telegram?.WebApp;
      if (telegram?.initDataUnsafe?.user) {
        return telegram.initDataUnsafe.user;
      }
    } catch (e) {
      console.error('Error accessing Telegram API:', e);
    }
    return null;
  };

  /**
   * Получение пользователя из URL параметров (для тестирования без Telegram)
   * Пример: ?userId=123&firstName=Роман&lastName=Хоменко
   */
  const extractUserFromUrl = () => {
    const params = new URLSearchParams(window.location.search);
    const userId = params.get('userId');
    if (userId) {
      return {
        id: parseInt(userId),
        first_name: params.get('firstName') || 'User',
        last_name: params.get('lastName') || '',
        username: params.get('username') || ''
      };
    }
    return null;
  };

  /**
   * Инициализация: определение пользователя
   * Сначала пытаемся получить из Telegram API, затем из URL
   */
  const init = async () => {
    console.log('Attempting to detect user...');
    console.log('Current URL:', window.location.href);
    console.log('Search params:', window.location.search);
    
    // Ждем инициализацию Telegram WebApp
    await new Promise(resolve => setTimeout(resolve, 100));
    
    // Пробуем разные способы получения пользователя
    let tgUser = getUserFromTelegramAPI();
    if (!tgUser) tgUser = extractUserFromUrl();
    
    if (tgUser && tgUser.id) {
      user.value = tgUser;
      // Список ID администраторов (можно вынести в конфиг)
      const adminIds = [123456789, 1003235952];
      isAdmin.value = adminIds.includes(tgUser.id);
      console.log('✅ User detected:', { id: tgUser.id, name: tgUser.first_name });
    } else {
      // Fallback для разработки без Telegram
      console.warn('❌ No user detected, using fallback');
      user.value = {
        id: 123456789,
        first_name: 'Тестовый',
        last_name: 'Пользователь',
        username: 'test'
      };
      isAdmin.value = true;
    }
    isLoading.value = false;
    ready.value = true;
  };

  onMounted(() => {
    init();
  });

  /** ID пользователя (число) */
  const userId = computed(() => user.value?.id || 0);
  
  /** Полное имя пользователя */
  const userName = computed(() => {
    if (!user.value) return 'Игрок';
    return `${user.value.first_name || ''} ${user.value.last_name || ''}`.trim() || user.value.username || 'Игрок';
  });

  return {
    user,       // Объект пользователя
    isAdmin,    // true если пользователь админ
    isLoading,  // true если данные загружаются
    ready,      // true если данные готовы
    userId,     // ID пользователя
    userName    // Имя пользователя
  };
}