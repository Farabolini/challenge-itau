FROM gradle:8.5-jdk21 AS gradle
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build -x test --no-daemon

FROM openjdk:21-slim AS app

ARG APP_VERSION=1.0.0
ENV APP_VERSION=$APP_VERSION

EXPOSE 8080
RUN mkdir /app
COPY --from=gradle /home/gradle/src/build/libs/challenge-itau-${APP_VERSION}.jar /app/challenge-itau.jar
ENTRYPOINT ["java","-jar","app/challenge-itau.jar"]