FROM maven:3.6.3-openjdk-17

RUN mkdir desc

WORKDIR desc

COPY . .

RUN mvn package -Dmaven.test.skip=true

CMD ["java", "-jar", "target/desc-1.0.0.jar"]