# Stage 1: Build dependencies
FROM eclipse-temurin:17-jdk-jammy as deps

WORKDIR /build

# Copy the mvnw wrapper with executable permissions.
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/

RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -DskipTests

################################################################################

# Stage 2: Package the application
FROM deps as package

WORKDIR /build

COPY ./src src/
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests && \
    mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

################################################################################

# Stage 3: Extract layers
FROM package as extract

WORKDIR /build

RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

################################################################################

# Stage 4: Final image with proxy and app
FROM eclipse-temurin:17-jre-jammy AS final

ARG UID=10001
RUN apt-get update && apt-get install -y squid && \
    adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

# Copy the executable layers
COPY --from=extract build/target/extracted/dependencies/ ./ 
COPY --from=extract build/target/extracted/spring-boot-loader/ ./ 
COPY --from=extract build/target/extracted/snapshot-dependencies/ ./ 
COPY --from=extract build/target/extracted/application/ ./ 

# Configure Squid proxy (basic setup)
COPY squid.conf /etc/squid/squid.conf

#EXPOSE 8003 3128  # App port (8003) + Proxy port (3128)
EXPOSE 8003  # App port
EXPOSE 3128  # Proxy port

CMD service squid start && java -jar app.jar