import axios, { type AxiosInstance, type AxiosError } from 'axios';
import type { Question, AnswerResponse, LeaderboardData } from '../types/game';

const API_BASE_URL = 'https://1443036d45e82aaf-178-68-29-212.serveousercontent.com/api';

class GameApi {
  private api: AxiosInstance;
  
  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 30000,
    });
    
    this.setupInterceptors();
  }
  
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
        
        if (error.response?.status === 401) {
          console.warn('Unauthorized access');
        } else if (error.response?.status === 404) {
          console.warn('Resource not found');
        } else if (error.code === 'ECONNABORTED') {
          console.error('Request timeout');
        }
        
        return Promise.reject(error);
      }
    );
  }
  
  async createSession(userId: number, chatId?: number): Promise<string> {
    const response = await this.api.post<string>(
      `/game/session?userId=${userId}&chatId=${chatId || userId}`
    );
    return response.data;
  }
  
  async getNextQuestion(sessionId: string): Promise<Question> {
    const response = await this.api.get<Question>(`/game/next-question?sessionId=${sessionId}`);
    return response.data;
  }
  
  async submitAnswer(sessionId: string, questionId: string, selectedOptionIndex: number): Promise<AnswerResponse> {
    const response = await this.api.post<AnswerResponse>('/game/answer', {
      sessionId,
      questionId,
      selectedOptionIndex
    });
    return response.data;
  }
  
  async getLeaderboard(week: string, userId?: number): Promise<LeaderboardData> {
    const response = await this.api.get<LeaderboardData>(
      `/leaderboard?week=${week}&userId=${userId || ''}`
    );
    return response.data;
  }
  
  async getGameStatus(sessionId: string): Promise<any> {
    const response = await this.api.get(`/game/status?sessionId=${sessionId}`);
    return response.data;
  }

  
  
  // Employee management
  async getEmployees(): Promise<any[]> {
    const response = await this.api.get('/employees');
    return response.data;
  }

  
  async createEmployee(data: any): Promise<any> {
    const response = await this.api.post('/employees', data);
    return response.data;
  }
  
  async updateEmployee(id: number, data: any): Promise<any> {
    const response = await this.api.put(`/employees/${id}`, data);
    return response.data;
  }
  
  async deleteEmployee(id: number): Promise<void> {
    await this.api.delete(`/employees/${id}`);
  }
  
  async toggleEmployeeActive(id: number, isActive: boolean): Promise<any> {
    const response = await this.api.patch(`/employees/${id}/active`, { isActive });
    return response.data;
  }

  async resetStats(userId: number): Promise<void> {
    const response = await this.api.post(`/game/reset-stats?userId=${userId}`);
    return response.data;
  }

  async getUserStats(userId: number): Promise<any> {
    const response = await this.api.get(`/user/stats?userId=${userId}`);
    return response.data;
  }

  async updateUserStats(userId: number, stats: {
    totalScore: number;
    correctAnswers: number;
    wrongAnswers: number;
    currentStreak: number;
    bestStreak: number;
  }): Promise<any> {
    const response = await this.api.post('/user/update-stats', null, {
      params: {
        userId,
        ...stats
      }
    });
    return response.data;
  }

  async getAllUsers(): Promise<any[]> {
    const response = await this.api.get('/user/all');
    return response.data;
  }

}

export const gameApi = new GameApi();
export default gameApi;