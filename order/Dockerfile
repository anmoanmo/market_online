FROM maven:3.8.8-eclipse-temurin-8 AS build
WORKDIR /app
COPY . .
RUN mvn package -pl order -am -DskipTests

FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/order/target/order-1.0-SNAPSHOT.jar app.jar
EXPOSE 9003
ENTRYPOINT ["java", "-jar", "app.jar"]
