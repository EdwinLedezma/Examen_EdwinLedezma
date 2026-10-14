FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY parcel-service/pom.xml parcel-service/pom.xml
COPY monitoring-service/pom.xml monitoring-service/pom.xml
COPY monitoring-service/src monitoring-service/src
RUN mvn -B -pl monitoring-service -am clean package
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/monitoring-service/target/monitoring-service-1.0.0.jar app.jar
USER 10001
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
