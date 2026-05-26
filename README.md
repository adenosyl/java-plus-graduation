# Explore With Me

Репозиторий содержит все этапы выполнения дипломного проекта Яндекс Практикума.

Дипломный проект курса Java Backend Developer: Сервис для публикации, поиска и участия в мероприятиях.

## Технологии

- Java
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Maven
- Docker

## Структура проекта

- `ewm-main-service` — основной сервис приложения;
- `ewm-stats-service` — сервис статистики;
- `docker-init` — скрипты инициализации БД;
- `postman` — коллекции запросов.

## Этапы разработки

### main
Исходная версия проекта, перенесённая из командного репозитория.

### spring-cloud
Подготовка приложения к работе в облачной среде:
- Config Server;
- Eureka Server;
- API Gateway.

### microservices
Переход к микросервисной архитектуре.

### recommendations
Реализация рекомендательной системы на основе Kafka и gRPC.

