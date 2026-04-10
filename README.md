# Угадай коллегу

Telegram мини-приложение для викторины "Угадай коллегу".

## О проекте

Приложение позволяет проверить, насколько хорошо вы знаете своих коллег. Нужно угадать сотрудника по фотографии или определить его отдел.

## Функционал

- Два режима игры: "Угадать сотрудника" и "Угадать отдел"
- Начисление очков за правильные ответы
- Серии правильных ответов
- Таблица лидеров
- Личная статистика
- Достижения
- Админ-панель для управления сотрудниками

## Технологии

### Backend
- Java 21
- Spring Boot 4.0.4
- PostgreSQL
- Maven

### Frontend
- Vue 3
- TypeScript
- Vite
- Pinia
- Axios

## Установка и запуск

### Требования
- Docker и Docker Compose
- Node.js 20+
- Java 21

### Запуск через Docker
docker-compose up -d

### Ручной запуск
Backend
bash
cd backend
./mvnw spring-boot:run
Frontend
bash
cd frontend
npm install
npm run dev

### Переменные окружения
Создайте файл .env в корне проекта:
env
POSTGRES_DB=guess_colleague
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin123
DB_HOST=localhost
DB_PORT=5432

### Telegram Mini App
Создайте бота через @BotFather
Отправьте команду /newapp
Укажите URL: https://ваш-домен.vercel.app
Запустите бота и нажмите "Open App"

### Структура базы данных
Таблица	Описание
telegram_users	Пользователи Telegram
employees	Сотрудники
game_sessions	Игровые сессии
question_attempts	Попытки ответов

### Деплой
Backend (Render.com)
Подключите GitHub репозиторий
Build Command: ./mvnw clean package
Start Command: java -jar target/*.jar

Frontend (Vercel)
Подключите GitHub репозиторий
Root Directory: frontend
Build Command: npm run build
Output Directory: dist
