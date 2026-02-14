# Используем минимальный runtime-образ с JDK 21
FROM eclipse-temurin:21-jre-alpine

# Создаём рабочую директорию приложения
WORKDIR /app

# Копируем jar-файл в контейнер
COPY target/payment-service-app-0.0.1-SNAPSHOT.jar app.jar

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]