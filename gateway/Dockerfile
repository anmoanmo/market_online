FROM maven:3.8.8-eclipse-temurin-8 AS build
WORKDIR /app
COPY . .
RUN mvn package -pl gateway -am -DskipTests

FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/gateway/target/gateway-1.0-SNAPSHOT.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
