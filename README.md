# Log Analyzer

Веб-платформа мониторинга логов в реальном времени, вдохновлённая Kibana и Grafana. Является пилотным пет-проектом

---

## Стек технологий

| Слой | Технология | Назначение |
|---|---|---|
| Язык | Java 25 | основной язык разработки |
| Фреймворк | Spring Boot 3.5 | веб-сервер, DI, конфигурация |
| База данных | PostgreSQL 16 | хранение логов и данных пользователей |
| Очередь сообщений | Apache Kafka | приём логов от внешних сервисов |
| Миграции БД | Flyway | версионирование схемы базы данных |
| ORM | Spring Data JPA / Hibernate | работа с базой данных |
| Шаблонизатор | Thymeleaf | серверный рендеринг HTML |
| Безопасность | Spring Security | аутентификация, хеширование паролей  |
| Графики | Chart.js | визуализация данных на дашборде |
| Drag-and-drop | SortableJS | перетаскивание виджетов |
| Контейнеризация | Docker  | запуск всего стека одной командой |
| Сборка | Apache Maven | управление зависимостями |
| Утилиты | Lombok | сокращение шаблонного кода |

---

## Возможности

- Дашборд с виджетами - статистика, графики, таблицы, датчики нагрузки (CPU / RAM / Disk)
- Живой поток логов с автообновлением всех виджетов
- Поиск и фильтрация по уровню, сервису, времени, тексту сообщения
- Пагинация с прямым переходом на нужную страницу
- Добавление, удаление и перетаскивание виджетов на дашборде
- Очистка логов - всех или по активному фильтру
- Регистрация и авторизация пользователей
- Профиль пользователя - имя, аватар, выбор языка интерфейса (EN / RU)
- Тёмная и светлая тема
- REST API для приёма логов от внешних приложений
- Приём логов через Kafka

---

## Быстрый старт

### Требования

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Git

### 1. Клонировать репозиторий

```bash
git clone https://github.com/username/log-analyzer.git
cd log-analyzer
```

### 2. Создать файл `.env`

```bash
cp .env.example .env
```

Открыть `.env` и заполнить значения:

```env
DB_HOST=postgres
DB_PORT=5432
DB_NAME=loganalyzer
DB_USER=loganalyzer
DB_PASSWORD=свой пароль

KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

### 3. Запустить

```bash
docker compose up -d
```

При первом запуске Docker скачает образы и соберёт приложение 

### 4. Открыть в браузере

```
http://localhost:8080
```

Требуется зарегистрировать аккаунт и войти в него - после этого откроется пустой дашборд.  
Нажмите **Demo Logs** для загрузки тестовых данных или **Start Stream** для живого потока. Нажав на кнопку с виджетами, вы сможете выбрать нужный для себя виджет

---

## Как открыть доступ другим пользователям

### Вариант 1: локальная сеть 

Подходит для демонстрации коллегам в одной сети.

1. Узнать свой локальный IP-адрес:
   - Windows: `ipconfig` — строка *IPv4 Address* 
   - macOS / Linux: `ip a` или `ifconfig`

2. Проверить, что брандмауэр разрешает входящие подключения на порт **8080**  

3. Поделиться адресом: `http://192.168.1.42:8080`

---

### Вариант 2: публичная ссылка через ngrok

Подходит для быстрого демо — работает без сервера, через любой интернет

1. Установить [ngrok](https://ngrok.com/) и зарегистрироваться там

2. Запустить приложение локально, затем в отдельном терминале ввести следующее:

```bash
ngrok http 8080
```

3. ngrok выдаст публичный адрес вида `https://abc123.ngrok-free.app` — им можно поделиться.

---

## Интеграция: отправка логов из своего приложения

Log Analyzer принимает логи через REST API, то есть настройки Kafka не нужно

### Endpoint

```
POST http://host_name:8080/api/logs
Content-Type: application/json
```

### Формат запроса

```json
{
  "timestamp":   "2025-06-01T12:00:00Z",
  "level":       "ERROR",
  "serviceName": "my-service",
  "message":     "Необработанная ошибка в PaymentController",
  "traceId":     "abc123def456",
  "host":        "prod-server-1"
}
```

| Поле          | Обязательное | Допустимые значения           |
|---------------|:---:|-------------------------------|
| `timestamp`   | Нет | ISO-8601; по умолчанию — текущее время |
| `level`       | Да  | `DEBUG` `INFO` `WARN` `ERROR` |
| `serviceName` | Да  | Строка до 100 символов        |
| `message`     | Да  | Любая строка                  |
| `traceId`     | Нет | До 64 символов                |
| `host`        | Нет | До 100 символов               |

---

## Запуск для разработки (без Docker)

Требования: **JDK 25**, **Docker Desktop** (только для Postgres и Kafka)

```bash
# Запустить только инфраструктуру
docker compose up postgres kafka -d

# Запустить приложение в IntelliJ IDEA
# Run Configuration -> Active profiles: local
```

Профиль `local` подключается к `localhost:5433` (Postgres) и `localhost:9094` (Kafka)

---

## Структура проекта

```
src/main/java/com/arssedot/loganalyzer/
├── config/             # Spring Security, Kafka
├── domain/             # JPA-сущности: LogEntry, User, Widget
├── kafka/              # Kafka producer и consumer
├── repository/         # Spring Data JPA репозитории
├── service/            # Бизнес-логика
└── web/
    ├── controller/     # REST и MVC контроллеры
    └── dto/            # DTO запросов и ответов

src/main/resources/
├── db/migration/       # Flyway SQL миграции 
├── templates/          # Thymeleaf шаблоны (dashboard, login, register)
└── application*.yml    # Конфигурация профилей (dev, local, docker)
```

---

## Переменные окружения

| Переменная | По умолчанию | Описание |
|---|---|---|
| `DB_HOST` | `postgres` | Хост PostgreSQL |
| `DB_PORT` | `5432` | Порт PostgreSQL |
| `DB_NAME` | `loganalyzer` | Название базы данных |
| `DB_USER` | `loganalyzer` | Пользователь базы данных |
| `DB_PASSWORD` | *(обязательно)* | Пароль |
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` | Адрес Kafka-брокера |

---

## Иллюстрации интерфейса

### 1. Настройки
<img width="857" height="786" alt="Снимок экрана 2026-03-22 014957" src="https://github.com/user-attachments/assets/3dc53062-0303-42ff-914d-38f219050b60" />


### 2. Панель виджетов
<img width="1397" height="876" alt="Снимок экрана 2026-03-22 015151" src="https://github.com/user-attachments/assets/f1997b95-94a0-4213-b391-c123ecd7ea97" />


### 3. Развернутые виджеты
<img width="2527" height="1178" alt="Снимок экрана 2026-03-22 015114" src="https://github.com/user-attachments/assets/f53c26fc-1e51-4bff-bb74-e61f14c4e84c" />


### 4. Таблица с логами
<img width="2524" height="1162" alt="Снимок экрана 2026-03-22 015136" src="https://github.com/user-attachments/assets/6e2b210a-59ef-476e-b92f-50734bcdff11" />




