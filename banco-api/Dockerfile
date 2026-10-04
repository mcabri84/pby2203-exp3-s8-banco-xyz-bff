FROM eclipse-temurin:17-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 app \
    && useradd --uid 10001 --gid app --create-home --shell /usr/sbin/nologin app \
    && mkdir -p /app /data && chown app:app /app /data
WORKDIR /app
COPY --chown=app:app target/*-SNAPSHOT.jar /app/app.jar
USER app
EXPOSE 8100
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
