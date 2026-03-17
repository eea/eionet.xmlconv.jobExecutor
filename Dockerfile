FROM eclipse-temurin:11-jdk-jammy
VOLUME /tmp
ARG ARTIFACT_NAME
COPY /target/app.jar /app.jar
ENV SPRING_PROFILES_ACTIVE docker
RUN bash -c 'touch /app.jar'
EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod","-jar","/app.jar"]



