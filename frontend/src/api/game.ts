import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export interface InitData {
  userId: number;
  chatId?: number;
  initData?: string;
}

export const gameApi = {
  // Создание сессии
  createSession: async (userId: number, chatId?: number) => {
    const response = await api.post<string>(`/game/session?userId=${userId}&chatId=${chatId || userId}`);
    return response.data;
  },

  // Получить следующий вопрос
  getNextQuestion: async (sessionId: string) => {
    const response = await api.get<Question>(`/game/next-question?sessionId=${sessionId}`);
    return response.data;
  },

  // Отправить ответ
  submitAnswer: async (sessionId: string, questionId: string, selectedOptionIndex: number) => {
    console.log('Submitting answer:', { sessionId, questionId, selectedOptionIndex });
    
    const response = await api.post<AnswerResponse>('/game/answer', {
      sessionId: sessionId,
      questionId: questionId,
      selectedOptionIndex: selectedOptionIndex
    });
    
    console.log('Answer response:', response.data);
    return response.data;
  },

  // Получить лидерборд
  getLeaderboard: async (week: string, userId?: number) => {
    const response = await api.get<LeaderboardData>(`/leaderboard?week=${week}&userId=${userId}`);
    return response.data;
  },

  // Получить статус игры
  getGameStatus: async (sessionId: string) => {
    const response = await api.get(`/game/status?sessionId=${sessionId}`);
    return response.data;
  },
  
};

export default gameApi;