# Build a minimal runtime image for the Java app
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the shaded (fat) jar produced by Maven Shade Plugin
COPY target/devops-pipeline-app-1.0.0-shaded.jar app.jar

# No ports to expose for a console app; uncomment if you later add HTTP server
# EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
