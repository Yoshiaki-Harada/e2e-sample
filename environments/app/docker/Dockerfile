# syntax = docker/dockerfile:1.1-experimental
FROM gradle:6.3.0-jdk13 as build-env
WORKDIR /work
COPY . .
RUN --mount=type=cache,target=/root/.gradle gradle build
RUN gradle shadowJar

FROM openjdk:15.0.2-jdk
COPY --from=build-env \
    /work/build/libs/*.jar \
    /usr/local/sns-api/
CMD [ "java", "-jar", "/usr/local/sns-api/sns-clone-by-tdd-1.0.0-all.jar" ]
