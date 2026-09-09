FROM eclipse-temurin:25-jre
WORKDIR /app
COPY trainer-workload-service/build/libs/trainer-workload-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
