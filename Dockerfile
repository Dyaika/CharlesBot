# Используем базовый образ OpenJDK 11
FROM openjdk:11-jre-slim
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
COPY config.json config.json
COPY charlesbot-acd4c-firebase-adminsdk-j1kjh-61e7ced5ee.json charlesbot-acd4c-firebase-adminsdk-j1kjh-61e7ced5ee.json
ENTRYPOINT ["java", "-jar", "/app.jar"]
