FROM openjdk:8-jdk
COPY src .
COPY . .
CMD ./gradlew assemble
COPY build /build


