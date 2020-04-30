# Use the official maven/Java 8 image to create a build artifact.
# https://hub.docker.com/_/maven
FROM fabric8/java-alpine-openjdk8-jre
#as builder

# Copy local code to the container image.
WORKDIR /app
#COPY pom.xml .
#COPY src ./src

# Build a release artifact.
#RUN mvn package -DskipTests

# Use AdoptOpenJDK for base image.
# It's important to use OpenJDK 8u191 or above that has container support enabled.
# https://hub.docker.com/r/adoptopenjdk/openjdk8
# https://docs.docker.com/develop/develop-images/multistage-build/#use-multi-stage-builds
#FROM adoptopenjdk/openjdk8:jdk8u202-b08-alpine-slim

# Copy the jar to the production image from the builder stage.
COPY target/MagNewsRESTController-*.jar /app/MagNewsRESTController-1.0.0.jar

EXPOSE 8080

# Run the web service on container startup.
CMD ["java","-jar","MagNewsRESTController-1.0.0.jar"]