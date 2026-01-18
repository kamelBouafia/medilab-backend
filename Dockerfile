# Build Stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Run Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG DEPENDENCY=/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app/classes

EXPOSE 8080
ENTRYPOINT ["java", "-cp", ".:classes:resources:lib/*", "com.medilab.MediLabProApplication"]
