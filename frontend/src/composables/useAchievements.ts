// composables/useAchievements.ts
import { ref } from 'vue';
import type { Store } from 'pinia';

interface Achievement {
  id: string;
  name: string;
  description: string;
  icon: string;
  condition: (store: any) => boolean;
  unlocked: boolean;
}

export function useAchievements() {
  const achievements = ref<Achievement[]>([
    {
      id: 'first_blood',
      name: 'Первая кровь',
      description: 'Правильно ответить на первый вопрос',
      icon: '🎯',
      condition: (store) => store.correctCount >= 1,
      unlocked: false
    },
    {
      id: 'streak_3',
      name: 'Набирая обороты',
      description: 'Серия из 3 правильных ответов',
      icon: '🔥',
      condition: (store) => store.bestStreak >= 3,
      unlocked: false
    },
    {
      id: 'streak_5',
      name: 'Горячая серия',
      description: 'Серия из 5 правильных ответов',
      icon: '⚡',
      condition: (store) => store.bestStreak >= 5,
      unlocked: false
    },
    {
      id: 'streak_10',
      name: 'Непобедимый',
      description: 'Серия из 10 правильных ответов',
      icon: '🏆',
      condition: (store) => store.bestStreak >= 10,
      unlocked: false
    },
    {
      id: 'score_100',
      name: 'Новичок',
      description: 'Набрать 100 очков',
      icon: '⭐',
      condition: (store) => store.score >= 100,
      unlocked: false
    },
    {
      id: 'score_500',
      name: 'Профессионал',
      description: 'Набрать 500 очков',
      icon: '🎖️',
      condition: (store) => store.score >= 500,
      unlocked: false
    },
    {
      id: 'score_1000',
      name: 'Эксперт',
      description: 'Набрать 1000 очков',
      icon: '🏅',
      condition: (store) => store.score >= 1000,
      unlocked: false
    },
    {
      id: 'accuracy_80',
      name: 'Меткий стрелок',
      description: 'Достичь точности 80%',
      icon: '🎯',
      condition: (store) => store.accuracy >= 80,
      unlocked: false
    },
    {
      id: 'questions_50',
      name: 'Любознательный',
      description: 'Ответить на 50 вопросов',
      icon: '📚',
      condition: (store) => store.totalQuestions >= 50,
      unlocked: false
    },
    {
      id: 'questions_100',
      name: 'Эрудит',
      description: 'Ответить на 100 вопросов',
      icon: '🧠',
      condition: (store) => store.totalQuestions >= 100,
      unlocked: false
    }
  ]);

  const loadUnlocked = () => {
    const saved = localStorage.getItem('achievements');
    if (saved) {
      try {
        const unlockedIds = JSON.parse(saved);
        achievements.value.forEach(ach => {
          ach.unlocked = unlockedIds.includes(ach.id);
        });
      } catch (e) {}
    }
  };

  const saveUnlocked = () => {
    const unlockedIds = achievements.value
      .filter(ach => ach.unlocked)
      .map(ach => ach.id);
    localStorage.setItem('achievements', JSON.stringify(unlockedIds));
  };

  const checkAchievements = (gameStore: any) => {
    let changed = false;
    achievements.value.forEach(ach => {
      if (!ach.unlocked && ach.condition(gameStore)) {
        ach.unlocked = true;
        changed = true;
        showAchievementNotification(ach);
      }
    });
    if (changed) {
      saveUnlocked();
    }
  };

  const showAchievementNotification = (achievement: Achievement) => {
    const notification = document.createElement('div');
    notification.className = 'achievement-notification';
    notification.innerHTML = `
      <div class="achievement-notification-content">
        <span class="achievement-icon">${achievement.icon}</span>
        <div class="achievement-info">
          <div class="achievement-title">Достижение разблокировано!</div>
          <div class="achievement-name">${achievement.name}</div>
          <div class="achievement-desc">${achievement.description}</div>
        </div>
      </div>
    `;
    document.body.appendChild(notification);
    
    setTimeout(() => {
      notification.classList.add('show');
    }, 100);
    
    setTimeout(() => {
      notification.classList.remove('show');
      setTimeout(() => {
        notification.remove();
      }, 300);
    }, 3000);
  };

  const getAchievements = (gameStore: any) => {
    checkAchievements(gameStore);
    return achievements.value;
  };

  const resetAchievements = () => {
    achievements.value.forEach(ach => {
      ach.unlocked = false;
    });
    saveUnlocked();
  };

  // Добавляем стили для уведомлений
  const addNotificationStyles = () => {
    if (document.getElementById('achievement-styles')) return;
    
    const style = document.createElement('style');
    style.id = 'achievement-styles';
    style.textContent = `
      .achievement-notification {
        position: fixed;
        bottom: 20px;
        right: 20px;
        transform: translateX(400px);
        transition: transform 0.3s ease;
        z-index: 10000;
      }
      
      .achievement-notification.show {
        transform: translateX(0);
      }
      
      .achievement-notification-content {
        background: linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%);
        border: 2px solid #ffd700;
        border-radius: 16px;
        padding: 12px 20px;
        display: flex;
        align-items: center;
        gap: 15px;
        box-shadow: 0 10px 30px rgba(0,0,0,0.3);
      }
      
      .achievement-icon {
        font-size: 40px;
      }
      
      .achievement-info {
        display: flex;
        flex-direction: column;
      }
      
      .achievement-title {
        font-size: 12px;
        color: #ffd700;
        font-weight: bold;
      }
      
      .achievement-name {
        font-size: 16px;
        color: white;
        font-weight: bold;
      }
      
      .achievement-desc {
        font-size: 11px;
        color: #888;
      }
    `;
    document.head.appendChild(style);
  };

  // Инициализация
  loadUnlocked();
  addNotificationStyles();

  return {
    achievements,
    checkAchievements,
    getAchievements,
    resetAchievements
  };
}