FROM openjdk:17

ARG JAR_FILE=/build/libs/*.jar

COPY ${JAR_FILE} community.jar

ENTRYPOINT ["java","-jar","community.jar"]
