# API Gateway Service

## Описание
API Gateway сервис предназначен для перенаправления запросов к различным микросервисам в вашем приложении. Он выступает в роли посредника, принимая все внешние запросы и направляя их к соответствующим внутренним сервисам. Это помогает уменьшить сложность клиентских приложений и централизовать общую логику, такую как аутентификация, логирование, кэширование и маршрутизация.

### Проблемы, которые решает API Gateway:
- **Централизованная маршрутизация**: API Gateway позволяет централизовать маршрутизацию всех входящих запросов к различным сервисам, обеспечивая единое входное место.
- **Безопасность и аутентификация**: Позволяет применять единые правила безопасности и аутентификации для всех входящих запросов.
- **Кэширование**: Может кэшировать ответы для уменьшения нагрузки на бэкэнд-сервисы.
- **Логирование и мониторинг**: Централизованное логирование и мониторинг всех запросов и ответов через один компонент.
- **Сокрытие внутренней структуры**: Скрывает внутреннюю структуру микросервисов от клиентов, предоставляя единый API.

## Требования
- Java 11
- Maven
- PostgreSQL
- Kafka

## Установка и запуск

1. Склонируйте репозиторий:
    ```bash
    git clone https://github.com/your-repo/api-gateway.git
    cd api-gateway
    ```

2. Настройте базу данных PostgreSQL и Kafka, обновив файл `application.yaml`.

3. Соберите и запустите приложение:
    ```bash
    mvn clean install
    java -jar target/apigateway-0.0.1-SNAPSHOT.jar
    ```

## Служебные эндпоинты Actuator

Spring Boot Actuator предоставляет следующие служебные эндпоинты для мониторинга и управления приложением. Все эндпоинты доступны по умолчанию через HTTP метод GET:

- **`/actuator/health`**: Показывает состояние здоровья приложения.
  - **Метод:** GET
  - **Описание:** Возвращает состояние здоровья приложения с подробностями.
  - **Пример запроса:** `GET http://localhost:8081/actuator/health`

- **`/actuator/info`**: Предоставляет информацию о приложении.
  - **Метод:** GET
  - **Описание:** Возвращает основную информацию о приложении.
  - **Пример запроса:** `GET http://localhost:8081/actuator/info`

- **`/actuator/metrics`**: Показывает метрики приложения.
  - **Метод:** GET
  - **Описание:** Возвращает метрики, такие как использование памяти, загруженность процессора и другие.
  - **Пример запроса:** `GET http://localhost:8081/actuator/metrics`

## Основные эндпоинты API Gateway

### Получить все редиректы
- **URL:** `/api/redirects`
- **Метод:** GET
- **Описание:** Возвращает список всех редиректов.
- **Пример запроса:**
    ```bash
    curl -X GET http://localhost:8081/api/redirects
    ```

### Добавить новый редирект
- **URL:** `/api/redirects`
- **Метод:** PUT
- **Описание:** Добавляет новый редирект.
- **Пример запроса:**
    ```bash
    curl -X PUT http://localhost:8081/api/redirects -H "Content-Type: application/json" -d '{"sourcePath":"/api/v1/service","targetUrl":"http://example.com","serviceName":"ExampleService","isLogging":true}'
    ```

### Обновить редирект
- **URL:** `/api/redirects`
- **Метод:** PATCH
- **Описание:** Обновляет существующий редирект.
- **Пример запроса:**
    ```bash
    curl -X PATCH http://localhost:8081/api/redirects -H "Content-Type: application/json" -d '{"sourcePath":"/api/v1/service","targetUrl":"http://example.org","serviceName":"ExampleService","isLogging":true}'
    ```

### Удалить редирект
- **URL:** `/api/redirects`
- **Метод:** DELETE
- **Описание:** Удаляет редирект по указанному пути.
- **Пример запроса:**
    ```bash
    curl -X DELETE http://localhost:8081/api/redirects?sourcePath=/api/v1/service
    ```

## Настройка и использование

### Шаги по настройке:

1. **Настройка базы данных PostgreSQL:**
   - Убедитесь, что у вас запущен экземпляр PostgreSQL.
   - Создайте базу данных и пользователя с соответствующими правами доступа.
   - Обновите параметры подключения к базе данных в файле `application.yaml`.

2. **Настройка Kafka:**
   - Убедитесь, что у вас запущен экземпляр Kafka.
   - Обновите параметры подключения к Kafka в файле `application.yaml`.

3. **Настройка сервера:**
   - Обновите конфигурацию сервера в файле `application.yaml`, если это необходимо (например, номер порта, параметры сжатия и т.д.).

### Как пользоваться:

1. **Запуск приложения:**
   - Запустите приложение командой:
     ```bash
     mvn clean install
     java -jar target/apigateway-0.0.1-SNAPSHOT.jar
     ```

2. **Работа с редиректами:**
   - **Добавление редиректа:**
     ```bash
     curl -X PUT http://localhost:8081/api/redirects -H "Content-Type: application/json" -d '{"sourcePath":"/api/v1/service","targetUrl":"http://example.com","serviceName":"ExampleService","isLogging":true}'
     ```
   - **Получение всех редиректов:**
     ```bash
     curl -X GET http://localhost:8081/api/redirects
     ```
   - **Обновление редиректа:**
     ```bash
     curl -X PATCH http://localhost:8081/api/redirects -H "Content-Type: application/json" -d '{"sourcePath":"/api/v1/service","targetUrl":"http://example.org","serviceName":"ExampleService","isLogging":true}'
     ```
   - **Удаление редиректа:**
     ```bash
     curl -X DELETE http://localhost:8081/api/redirects?sourcePath=/api/v1/service
     ```

3. **Мониторинг и управление:**
   - **Проверка состояния здоровья:**
     ```bash
     curl -X GET http://localhost:8081/actuator/health
     ```
   - **Получение информации о приложении:**
     ```bash
     curl -X GET http://localhost:8081/actuator/info
     ```
   - **Получение метрик:**
     ```bash
     curl -X GET http://localhost:8081/actuator/metrics
     ```