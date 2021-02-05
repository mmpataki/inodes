FROM openjdk:8-jdk-alpine
#FROM tomcat:latest
ARG JAR=target/*.jar
ARG CONFIG=config.properties

COPY ${JAR} /app.jar
COPY ${CONFIG} /config.properties
ADD klasses /klasses
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
