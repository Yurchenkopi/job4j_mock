FROM maven:3.6.3-openjdk-17

RUN mkdir notification

WORKDIR notification

COPY . .

RUN mvn package -Dmaven.test.skip=true

CMD ["java", "-jar", "target/notification-1.0.0.jar"]