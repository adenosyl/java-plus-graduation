FROM eclipse-temurin:21-jre

WORKDIR /app

COPY ewm-main-service/target/ewm-main-service-*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]