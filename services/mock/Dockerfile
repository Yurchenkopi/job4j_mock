FROM maven:3.6.3-openjdk-17

RUN mkdir mock

WORKDIR mock

COPY . .

RUN mvn package -Dmaven.test.skip=true

CMD ["java", "-jar", "target/mock-1.0.0.jar"]