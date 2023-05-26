FROM openjdk:17
ADD backend/backend/target/sutoga.jar sutoga.jar
EXPOSE 8080
ENTRYPOINT ["java","-Djava.sdockerecurity.egd=file:/dev/./urandom","-jar","/sutoga.jar"]
