FROM eclipse-temurin:25-jre
WORKDIR /app
COPY discovery-service/build/libs/discovery-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
