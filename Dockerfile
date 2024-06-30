#
# Build stage
#
FROM maven:3.8.7-openjdk-18-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

#
# Package stage
#
FROM openjdk:18-jdk-slim
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean
WORKDIR /app
VOLUME /vtk-static
RUN mkdir -p /app/static/rtsp \
    && mkdir -p /app/static/csv
COPY --from=build /app/target/rtsp-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
