# Explore With Me
 — это платформа для организации мероприятий, регистрации, участия, комментирования.

## Архитектура
- Проект построен на микросервисной архитектуре с использованием Spring Cloud и включает следующие компоненты:

## Инфраструктурные компоненты
- <b>discovery-server</b> — сервис регистрации и обнаружения микросервисов на базе Eureka, http://localhost:8761
- <b>config-server</b>  — сервис конфигураций, централизованное управление настройками.
- <b>gateway-server</b> — API-шлюз на базе Spring Cloud Gateway — единая точка входа для всех клиентов, http://localhost:8080

## Бизнес-сервисы
 (Ииспользуют динамические порты)<br>
- <b>user-service</b> — управление пользователями.
- <b>event-service</b> — управление событиями, категориями и подборками
- <b>request-service</b> — управление запросами на участие в событиях
- <b>comment-service</b> — управление комментариями к событиям
- <b>stats-server</b> — сервис статистики просмотров событий

## Внутренний API<br>
Взаимодействие между микросервисами осуществляется через <b>FeignClient</b><br>
Клиенты находятся в модуле <b>interaction-api</b> и имеют fallback

## Спецификации внешних API
```
https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-main-service-spec.json

https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-stats-service-spec.json
```