# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# cache dependencies
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# build
COPY src ./src
RUN mvn -q -DskipTests package

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Render uses PORT; expose is informational but nice
EXPOSE 8080

# copy jar (Spring Boot fat jar)
COPY --from=build /app/target/*.jar app.jar

# optional: smaller memory footprint / container friendliness
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
