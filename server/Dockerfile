FROM maven:3.8.8-eclipse-temurin-8 AS build

WORKDIR /workspace

ARG APP_MODULE
ARG APP_JAR

COPY pom.xml .
COPY data-server-runtime/pom.xml data-server-runtime/pom.xml
COPY data-server-admin-app/pom.xml data-server-admin-app/pom.xml
COPY data-server-gateway-app/pom.xml data-server-gateway-app/pom.xml
RUN mvn -B -pl ${APP_MODULE} -am dependency:go-offline

COPY . .
RUN mvn -B -pl ${APP_MODULE} -am -DskipTests package

FROM eclipse-temurin:8-jre

WORKDIR /app

ARG APP_MODULE
ARG APP_JAR

ENV JAVA_OPTS=""

COPY --from=build /workspace/${APP_MODULE}/target/${APP_JAR} /app/app.jar

EXPOSE 8080 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
