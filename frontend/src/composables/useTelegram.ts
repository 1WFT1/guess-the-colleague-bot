// frontend/src/composables/useTelegram.ts
import { ref, computed, onMounted } from 'vue';

export function useTelegram() {
  const user = ref<any>(null);
  const isAdmin = ref(false);
  const isLoading = ref(true);

  const getTelegramUser = () => {
    // Проверяем несколько возможных источников данных
    const telegram = (window as any).Telegram?.WebApp;
    
    // Способ 1: через WebApp
    if (telegram?.initDataUnsafe?.user) {
      return telegram.initDataUnsafe.user;
    }
    
    // Способ 2: через URL параметры (для тестирования)
    const urlParams = new URLSearchParams(window.location.search);
    const userId = urlParams.get('userId');
    if (userId) {
      return {
        id: parseInt(userId),
        first_name: urlParams.get('firstName') || 'Test',
        last_name: urlParams.get('lastName') || '',
        username: urlParams.get('username') || ''
      };
    }
    
    return null;
  };

  const initTelegram = () => {
    const telegram = (window as any).Telegram?.WebApp;
    if (telegram) {
      telegram.ready();
      telegram.expand();
      console.log('Telegram WebApp initialized:', telegram.initDataUnsafe);
    }
  };

  onMounted(() => {
    initTelegram();
    const tgUser = getTelegramUser();
    
    if (tgUser && tgUser.id) {
      user.value = tgUser;
      // Проверка на админа по ID
      const adminIds = [123456789, 1003235952]; // Добавьте ID админов
      isAdmin.value = adminIds.includes(tgUser.id);
      console.log('Telegram user detected:', { id: tgUser.id, name: tgUser.first_name });
    } else {
      // Fallback для разработки
      console.warn('No Telegram user detected, using fallback');
      user.value = {
        id: 123456789,
        first_name: 'Тестовый',
        last_name: 'Пользователь',
        username: 'test'
      };
      isAdmin.value = true;
    }
    isLoading.value = false;
  });

  const userId = computed(() => user.value?.id || 0);
  const userName = computed(() => {
    if (!user.value) return 'Игрок';
    return `${user.value.first_name || ''} ${user.value.last_name || ''}`.trim() || user.value.username || 'Игрок';
  });

  return {
    user,
    isAdmin,
    isLoading,
    userId,
    userName
  };
}