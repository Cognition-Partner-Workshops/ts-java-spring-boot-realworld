FROM eclipse-temurin:11-jdk-alpine as builder

WORKDIR /app

COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081

ENV JAVA_OPTS=""
ENV JWT_SESSION_TIME="86400"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
