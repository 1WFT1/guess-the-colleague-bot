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