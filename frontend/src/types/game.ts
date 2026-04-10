/**
 * Типы и интерфейсы для игры "Угадай коллегу"
 */

// ========== ИГРОВЫЕ ТИПЫ ==========
export interface Question {
  questionId: string;
  photoUrl: string | null;
  options: string[];
  mode?: 'name' | 'department';
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

// ========== СТАТИСТИКА ==========
export interface GameStats {
  score: number;
  correctCount: number;
  wrongCount: number;
  currentStreak: number;
  bestStreak: number;
  lastUpdated: string | null;
}

// ========== СОТРУДНИКИ ==========
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