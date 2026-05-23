FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /opt/app
COPY .mvn/ .mvn
COPY mvnw pom.xml settings.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -s settings.xml
COPY ./src ./src
RUN ./mvnw clean install -DskipTests -s settings.xml

FROM eclipse-temurin:21-jre-alpine
WORKDIR /opt/app
COPY --from=builder /opt/app/target/*.jar /opt/app/*.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/opt/app/*.jar"]