// frontend/src/composables/useTelegram.ts
import { ref, computed, onMounted } from 'vue';

export function useTelegram() {
  const user = ref<any>(null);
  const isAdmin = ref(false);
  const isLoading = ref(true);

  const getUserFromUrl = () => {
    // Пытаемся получить данные из URL параметров
    const urlParams = new URLSearchParams(window.location.search);
    
    // Проверяем параметры от Telegram Web
    const tgWebAppData = urlParams.get('tgWebAppData');
    if (tgWebAppData) {
      try {
        const decoded = decodeURIComponent(tgWebAppData);
        const params = new URLSearchParams(decoded);
        const userJson = params.get('user');
        if (userJson) {
          const userData = JSON.parse(decodeURIComponent(userJson));
          return {
            id: userData.id,
            first_name: userData.first_name,
            last_name: userData.last_name,
            username: userData.username
          };
        }
      } catch (e) {
        console.error('Failed to parse tgWebAppData:', e);
      }
    }
    
    // Fallback: прямые параметры
    const userId = urlParams.get('userId');
    if (userId) {
      return {
        id: parseInt(userId),
        first_name: urlParams.get('firstName') || 'User',
        last_name: urlParams.get('lastName') || '',
        username: urlParams.get('username') || ''
      };
    }
    
    return null;
  };

  const getUserFromTelegram = () => {
    const telegram = (window as any).Telegram?.WebApp;
    if (telegram?.initDataUnsafe?.user) {
      return telegram.initDataUnsafe.user;
    }
    return null;
  };

  onMounted(() => {
    // Сначала пробуем получить из Telegram API
    let tgUser = getUserFromTelegram();
    
    // Если не получилось - пробуем из URL (для Telegram Web)
    if (!tgUser) {
      tgUser = getUserFromUrl();
    }
    
    if (tgUser && tgUser.id) {
      user.value = tgUser;
      const adminIds = [123456789, 1003235952];
      isAdmin.value = adminIds.includes(tgUser.id);
      console.log('User detected:', { id: tgUser.id, name: tgUser.first_name });
    } else {
      console.warn('No user detected, using fallback');
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