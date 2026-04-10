/**
 * API клиент для взаимодействия с бэкендом
 * Все запросы к серверу проходят через этот класс
 */

import axios, { type AxiosInstance, type AxiosError } from 'axios';
import type { Question, AnswerResponse } from '../types/game';

// Базовый URL API (из переменных окружения или localhost для разработки)
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

class GameApi {
  private api: AxiosInstance;
  
  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: { 'Content-Type': 'application/json' },
      timeout: 30000,
    });
    
    this.setupInterceptors();
  }
  
  /** Обновить режим игры в существующей сессии */
  async updateGameMode(sessionId: string, gameMode: string): Promise<void> {
    const response = await this.api.patch(`/game/session/${sessionId}/mode?gameMode=${gameMode}`);
    return response.data;
  }

  /** Настройка перехватчиков запросов/ответов для логирования */
  private setupInterceptors() {
    this.api.interceptors.request.use(
      (config) => {
        console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`);
        return config;
      },
      (error) => {
        console.error('[API Request Error]', error);
        return Promise.reject(error);
      }
    );
    
    this.api.interceptors.response.use(
      (response) => {
        console.log(`[API Response] ${response.config.url}`, response.status);
        return response;
      },
      (error: AxiosError) => {
        console.error('[API Response Error]', error.message);
        
        if (error.response?.status === 401) console.warn('Unauthorized access');
        else if (error.response?.status === 404) console.warn('Resource not found');
        else if (error.code === 'ECONNABORTED') console.error('Request timeout');
        
        return Promise.reject(error);
      }
    );
  }

  /** Сбросить счетчик игр пользователя */
  async resetGamesPlayed(userId: number): Promise<void> {
    const response = await this.api.post(`/user/reset-games-played?userId=${userId}`);
    return response.data;
  }
  
  /** Создать новую игровую сессию */
  async createSession(userId: number, chatId?: number, gameMode?: 'name' | 'department'): Promise<string> {
    const response = await this.api.post<string>(
      `/game/session?userId=${userId}&chatId=${chatId || userId}&gameMode=${gameMode || 'name'}`
    );
    return response.data;
  }
  
  /** Получить следующий вопрос */
  async getNextQuestion(sessionId: string): Promise<Question> {
    const response = await this.api.get<Question>(`/game/next-question?sessionId=${sessionId}`);
    return response.data;
  }
  
  /** Отправить ответ на вопрос */
  async submitAnswer(sessionId: string, questionId: string, selectedOptionIndex: number): Promise<AnswerResponse> {
    const response = await this.api.post<AnswerResponse>('/game/answer', {
      sessionId,
      questionId,
      selectedOptionIndex
    });
    return response.data;
  }
  
  /** Получить статус игровой сессии */
  async getGameStatus(sessionId: string): Promise<any> {
    const response = await this.api.get(`/game/status?sessionId=${sessionId}`);
    return response.data;
  }

  // ========== УПРАВЛЕНИЕ СОТРУДНИКАМИ ==========
  
  /** Получить всех сотрудников (для админ-панели) */
  async getEmployees(): Promise<any[]> {
    const response = await this.api.get('/employees');
    return response.data;
  }

  /** Получить только активных сотрудников (для игры) */
  async getActiveEmployees(): Promise<any[]> {
    const response = await this.api.get('/employees/active');
    return response.data;
  }

  /** Создать нового сотрудника */
  async createEmployee(data: any): Promise<any> {
    const response = await this.api.post('/employees', data);
    return response.data;
  }
  
  /** Обновить данные сотрудника */
  async updateEmployee(id: number, data: any): Promise<any> {
    const response = await this.api.put(`/employees/${id}`, data);
    return response.data;
  }
  
  /** Удалить сотрудника */
  async deleteEmployee(id: number): Promise<void> {
    await this.api.delete(`/employees/${id}`);
  }
  
  /** Переключить статус активности сотрудника */
  async toggleEmployeeActive(id: number, active: boolean): Promise<any> {
    const response = await this.api.patch(`/employees/${id}/active`, { active });
    return response.data;
  }

  // ========== СТАТИСТИКА ПОЛЬЗОВАТЕЛЕЙ ==========

  /** Сбросить всю статистику пользователя */
  async resetStats(userId: number): Promise<void> {
    const response = await this.api.post(`/game/reset-stats?userId=${userId}`);
    return response.data;
  }

  /** Получить статистику пользователя */
  async getUserStats(userId: number): Promise<any> {
    const response = await this.api.get(`/user/stats?userId=${userId}`);
    return response.data;
  }

  /** Обновить статистику пользователя после игры */
  async updateUserStats(userId: number, stats: {
    totalScore: number;
    correctAnswers: number;
    wrongAnswers: number;
    currentStreak: number;
    bestStreak: number;
  }): Promise<any> {
    const response = await this.api.post('/user/update-stats', null, {
      params: { userId, ...stats }
    });
    return response.data;
  }

  /** Получить всех пользователей (для лидерборда) */
  async getAllUsers(): Promise<any[]> {
    const response = await this.api.get('/user/all');
    return response.data;
  }
}

export const gameApi = new GameApi();
export default gameApi;