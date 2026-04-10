// composables/useTelegram.ts
import { ref, computed, onMounted } from 'vue';

export function useTelegram() {
  const user = ref<any>(null);
  const isAdmin = ref(false);
  const isLoading = ref(true);
  const ready = ref(false);  

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

  const init = async () => {
    console.log('Attempting to detect user...');
    console.log('Current URL:', window.location.href);
    console.log('Search params:', window.location.search);
    
    // Даем время Telegram WebApp на инициализацию
    await new Promise(resolve => setTimeout(resolve, 100));
    
    // Пробуем все способы
    let tgUser = getUserFromTelegramAPI();
    if (!tgUser) tgUser = extractUserFromUrl();
    
    if (tgUser && tgUser.id) {
      user.value = tgUser;
      const adminIds = [123456789, 1003235952];
      isAdmin.value = adminIds.includes(tgUser.id);
      console.log('✅ User detected:', { id: tgUser.id, name: tgUser.first_name });
    } else {
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

  const userId = computed(() => user.value?.id || 0);
  const userName = computed(() => {
    if (!user.value) return 'Игрок';
    return `${user.value.first_name || ''} ${user.value.last_name || ''}`.trim() || user.value.username || 'Игрок';
  });

  return {
    user,
    isAdmin,
    isLoading,
    ready, 
    userId,
    userName
  };
}