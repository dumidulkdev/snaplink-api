FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY target/snaplink-api-*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]