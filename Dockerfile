# ============================================================
# DisasterHelp API - Dockerfile multi-stage
# Estagio 1: build do .jar com Maven (gera imagem personalizada)
# Estagio 2: runtime enxuto, usuario nao privilegiado
# ============================================================

# ---------- Estagio 1: BUILD ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Cache de dependencias: copia so o pom primeiro
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Copia o codigo-fonte e empacota (pula testes para o build do container)
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---------- Estagio 2: RUNTIME ----------
FROM eclipse-temurin:17-jre

# Diretorio de trabalho (nomeacao livre)
WORKDIR /opt/disasterhelp

# Usuario nao privilegiado (nome livre)
RUN useradd -u 1001 -m -s /bin/bash disasteruser

# Variaveis de ambiente da aplicacao
ENV APP_NAME=DisasterHelpAPI \
    SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-db:5432/disasterdb \
    SPRING_DATASOURCE_USERNAME=postgres \
    SPRING_DATASOURCE_PASSWORD=postgres123 \
    SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver \
    SPRING_JPA_HIBERNATE_DDL_AUTO=update \
    SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect

# Copia o artefato gerado no estagio de build
COPY --from=build /build/target/*.jar app.jar

# Ajusta dono e roda como usuario nao-root
RUN chown disasteruser:disasteruser app.jar
USER disasteruser

# Porta exposta para acesso a aplicacao
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
