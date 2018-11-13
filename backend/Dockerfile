# Base Alpine Linux based image with OpenJDK JRE only
FROM openjdk:8-jre-alpine
# copy application JAR (with libraries inside)
COPY target/locationReciever-*.jar /app.jar
# Set to expose port 8080
EXPOSE 8080
# start .jar application
CMD ["/usr/bin/java", "-jar", "/app.jar"]