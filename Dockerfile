FROM gradle:6-jdk11 AS build
USER gradle
WORKDIR /home/gradle/project
COPY build.gradle settings.gradle gradle.properties ./
RUN gradle dependencies
COPY . .
RUN gradle installDist

FROM adoptopenjdk:11-jre
RUN mkdir /opt/app
WORKDIR /opt/app
COPY --from=build /home/gradle/project/build/install/http2-sse-example .
CMD ["bin/http2-sse-example"]
