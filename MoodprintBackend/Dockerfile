FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
RUN groupadd --system moodprint && useradd --system --gid moodprint --home-dir /app moodprint
WORKDIR /app
COPY --from=builder /workspace/build/libs/moodprint-backend-*.jar /app/app.jar
# The default local profile persists H2 under /app/data. Keep the image runnable as
# the unprivileged user without granting write access to the jar itself.
RUN mkdir -p /app/data && chown moodprint:moodprint /app/data && chown moodprint:moodprint /app/app.jar

USER moodprint
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
