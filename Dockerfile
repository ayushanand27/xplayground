# Build a minimal runtime image for the Java app
# eclipse-temurin is the official successor to the deprecated openjdk Docker images
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the shaded (fat) jar produced by Maven Shade Plugin
# Shade plugin replaces the original jar in-place, so the file is devops-pipeline-app-1.0.0.jar
COPY target/devops-pipeline-app-1.0.0.jar app.jar

# Expose the application HTTP port
EXPOSE 8800

ENTRYPOINT ["java","-jar","/app/app.jar"]
