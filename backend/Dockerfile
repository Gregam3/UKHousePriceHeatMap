# Base Alpine Linux based image with OpenJDK JRE only
FROM openjdk:8-jre-alpine
# copy application JAR (with libraries inside)
COPY target/locationReciever-*.jar /app.jar
# start .jar application
CMD ["/usr/bin/java", "-jar", "/app.jar"]