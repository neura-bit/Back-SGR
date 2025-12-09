FROM eclipse-temurin:17-jdk-alpine

LABEL org.opencontainers.image.source="https://github.com/neura-bit/Back-SGR"
LABEL org.opencontainers.image.title="seguimiento-mensajeros"
LABEL org.opencontainers.image.description="Backend Seguimiento Mensajeros - Spring Boot JAR"
LABEL org.opencontainers.image.licenses="Proprietary"
LABEL org.opencontainers.image.version="latest"

# Zona horaria
ENV TZ=America/Guayaquil
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

# Ajustar al nombre real del jar generado
ARG JAR_FILE=target/seguimiento-mensajeros-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# JVM
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Dfile.encoding=UTF-8 -Duser.timezone=America/Guayaquil"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -jar app.jar"]
