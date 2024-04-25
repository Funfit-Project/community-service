FROM openjdk:17

ARG JAR_FILE=/build/libs/*.jar

# /build/libs 경로에 있는 모든 jar 파일을 도커 이미지 내부로 족사
COPY ${JAR_FILE} funfit_community.jar

# 도커 컨테이너가 실행될 때 자동으로 실행될 명령어
ENTRYPOINT ["java","-jar","funfit_community.jar"]
