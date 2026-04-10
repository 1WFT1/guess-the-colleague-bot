// types/game.ts
export interface Question {
  questionId: string;
  photoUrl: string | null;
  options: string[];
}

export interface AnswerRequest {
  sessionId: string;
  questionId: string;
  selectedOptionIndex: number;
}

export interface AnswerResponse {
  correct: boolean;
  pointsDelta: number;
  newTotalScore: number;
  correctAnswer: string;
  message: string;
}

export interface GameSession {
  id: string;
  userId: number;
  totalScore: number;
  correctAnswers: number;
  wrongAnswers: number;
}

export interface LeaderboardEntry {
  userId: number;
  fullName: string;
  totalScore: number;
  accuracy: number;
  rank: number;
}

export interface LeaderboardData {
  week: string;
  entries: LeaderboardEntry[];
  currentUser: LeaderboardEntry | null;
}

export interface GameStats {
  score: number;
  correctCount: number;
  wrongCount: number;
  currentStreak: number;
  bestStreak: number;
  lastUpdated: string | null;
}

export interface PlayerStats {
  userId: number;
  fullName: string;
  totalScore: number;
  accuracy: number;
  correctCount: number;
  wrongCount: number;
  lastUpdated: string;
}

export type WeekDay = 'Пн' | 'Вт' | 'Ср' | 'Чт' | 'Пт' | 'Сб' | 'Вс';

// Исправляем интерфейс WeeklyStats - убираем конфликтующие свойства
export interface WeeklyStats {
  Пн: number;
  Вт: number;
  Ср: number;
  Чт: number;
  Пт: number;
  Сб: number;
  Вс: number;
}

export interface WeeklyStatsWithMeta extends WeeklyStats {
  weekKey: string;
  totalScore: number;
}

export interface Employee {
  id: number;
  fullName: string;
  department: string;
  photoUrl: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface EmployeeForm {
  fullName: string;
  department: string;
  photoUrl: string;
  active: boolean;
}