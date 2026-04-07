// composables/useTelegram.ts
import { ref, computed, onMounted } from 'vue';

export interface TelegramUser {
  id: number;
  firstName: string;
  lastName: string;
  username?: string;
  fullName: string;
}

export function useTelegram() {
  const user = ref<TelegramUser | null>(null);
  const isAdmin = ref(false);
  const isLoading = ref(true);

  const getTelegramUser = (): TelegramUser | null => {
    const telegram = (window as any).Telegram?.WebApp;
    if (!telegram?.initDataUnsafe?.user) return null;

    const tgUser = telegram.initDataUnsafe.user;
    return {
      id: tgUser.id,
      firstName: tgUser.first_name || '',
      lastName: tgUser.last_name || '',
      username: tgUser.username,
      fullName: `${tgUser.first_name || ''} ${tgUser.last_name || ''}`.trim() || 'Игрок'
    };
  };

  const initTelegram = () => {
    const telegram = (window as any).Telegram?.WebApp;
    if (telegram) {
      telegram.ready();
      telegram.expand();
    }
  };

  onMounted(() => {
    initTelegram();
    const tgUser = getTelegramUser();
    if (tgUser) {
      user.value = tgUser;
      isAdmin.value = tgUser.username === 'admin' || tgUser.id === 123456789;
    } else {
      // Fallback для разработки
      user.value = {
        id: 123456789,
        firstName: 'Тестовый',
        lastName: 'Пользователь',
        username: 'test',
        fullName: 'Тестовый Пользователь'
      };
      isAdmin.value = true;
    }
    isLoading.value = false;
  });

  // Вычисляемые свойства для удобства
  const userId = computed(() => user.value?.id || 0);
  const userName = computed(() => user.value?.fullName || 'Игрок');
  const userInitials = computed(() => {
    const name = user.value?.fullName || '';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      const first = parts[0]?.[0] || '';
      const second = parts[1]?.[0] || '';
      if (first && second) return (first + second).toUpperCase();
    }
    return name[0]?.toUpperCase() || '👤';
  });

  return {
    user,
    isAdmin: computed(() => isAdmin.value),
    isLoading: computed(() => isLoading.value),
    userId,
    userName,
    userInitials
  };
}