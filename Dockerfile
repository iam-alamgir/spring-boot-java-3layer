FROM maven:3.9.10-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
COPY README.md ./README.md
RUN mvn -q -DskipTests package

FROM gcr.io/distroless/java25-debian12:nonroot
WORKDIR /app
COPY --from=build /workspace/target/spring-boot-java-3layer-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
