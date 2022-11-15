FROM openjdk:18.0.2.1-slim

COPY build/expense-tracker-api-*.jar /expense-tracker-api.jar

ENTRYPOINT ["java", "-jar", "/expense-tracker-api.jar"]