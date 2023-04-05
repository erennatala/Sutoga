FROM openjdk:17
VOLUME /tmp
ADD target/backend-1.0.jar backend-1.0.jar
EXPOSE 3000
ENTRYPOINT ["java","-Djava.sdockerecurity.egd=file:/dev/./urandom","-jar","/backend-1.0.jar"]