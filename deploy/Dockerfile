FROM openjdk:17.0.2-slim

COPY build/expense-tracker-api-*.jar /expense-tracker-api.jar

ENTRYPOINT ["java", "-jar", "/expense-tracker-api.jar"]