FROM --platform=$BUILDPLATFORM gradle:jdk17 AS build
WORKDIR /build
COPY . .
RUN gradle wrapper
RUN ./gradlew build -x test

FROM openjdk:17-oracle
WORKDIR /app
VOLUME /tmp
COPY --from=build /build/build/libs/Emercast*SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]