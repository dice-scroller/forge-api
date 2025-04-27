FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# This feels bad
COPY ./forge-gui/res ./forge-gui/res

# Copy the final JAR from forge-api module
COPY ./forge-api/target/forge-api-0.0.1.jar forge-api.jar

EXPOSE 7000

ENTRYPOINT ["java", "-jar", "forge-api.jar"]
