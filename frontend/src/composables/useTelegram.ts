// frontend/src/composables/useTelegram.ts
import { ref, computed, onMounted } from 'vue';

export function useTelegram() {
  const user = ref<any>(null);
  const isAdmin = ref(false);
  const isLoading = ref(true);

  const extractUserFromUrl = () => {
    const url = window.location.href;
    const params = new URLSearchParams(window.location.search);
    
    // Способ 1: Прямые параметры
    let userId = params.get('userId');
    let firstName = params.get('firstName');
    let lastName = params.get('lastName');
    let username = params.get('username');
    
    if (userId) {
      return { id: parseInt(userId), first_name: firstName, last_name: lastName, username };
    }
    
    // Способ 2: Параметр tgWebAppData
    const tgWebAppData = params.get('tgWebAppData');
    if (tgWebAppData) {
      try {
        const decoded = decodeURIComponent(tgWebAppData);
        const dataParams = new URLSearchParams(decoded);
        const userJson = dataParams.get('user');
        if (userJson) {
          const userData = JSON.parse(decodeURIComponent(userJson));
          return userData;
        }
      } catch (e) {
        console.error('Failed to parse tgWebAppData:', e);
      }
    }
    
    // Способ 3: Из hash
    if (window.location.hash) {
      const hashParams = new URLSearchParams(window.location.hash.substring(1));
      const hashUserId = hashParams.get('userId');
      if (hashUserId) {
        return {
          id: parseInt(hashUserId),
          first_name: hashParams.get('firstName'),
          last_name: hashParams.get('lastName'),
          username: hashParams.get('username')
        };
      }
    }
    
    return null;
  };

  const getUserFromTelegramAPI = () => {
    try {
      const telegram = (window as any).Telegram?.WebApp;
      if (telegram?.initDataUnsafe?.user) {
        return telegram.initDataUnsafe.user;
      }
      
      // Альтернативный способ через WebApp
      if (telegram?.WebApp?.initDataUnsafe?.user) {
        return telegram.WebApp.initDataUnsafe.user;
      }
    } catch (e) {
      console.error('Error accessing Telegram API:', e);
    }
    return null;
  };

  const getUserFromWindow = () => {
    // Проверяем глобальные объекты
    const possiblePaths = [
      'Telegram.WebApp.initDataUnsafe.user',
      'Telegram.WebApp.user',
      'Telegram.user',
      'WebApp.initDataUnsafe.user',
      'WebApp.user'
    ];
    
    for (const path of possiblePaths) {
      try {
        const parts = path.split('.');
        let obj = window as any;
        for (const part of parts) {
          obj = obj[part];
          if (!obj) break;
        }
        if (obj && obj.id) {
          return obj;
        }
      } catch (e) {
        // continue
      }
    }
    return null;
  };

  onMounted(() => {
    console.log('Attempting to detect user...');
    console.log('Current URL:', window.location.href);
    console.log('Search params:', window.location.search);
    
    // Пробуем все способы
    let tgUser = getUserFromTelegramAPI();
    if (!tgUser) tgUser = getUserFromWindow();
    if (!tgUser) tgUser = extractUserFromUrl();
    
    if (tgUser && tgUser.id) {
      user.value = tgUser;
      const adminIds = [123456789, 1003235952];
      isAdmin.value = adminIds.includes(tgUser.id);
      console.log('✅ User detected:', { id: tgUser.id, name: tgUser.first_name });
    } else {
      console.warn('❌ No user detected, using fallback');
      console.log('Window.Telegram:', (window as any).Telegram);
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