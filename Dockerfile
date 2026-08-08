# ------------------------------------------
# Etapa 1: build do jar com Maven (self-contained, nao depende de
# Maven/JDK instalados no host)
# ------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copia primeiro so o pom.xml para aproveitar cache de dependencias entre builds
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

# ------------------------------------------
# Etapa 2: imagem final, so com o JRE e o jar gerado
# ------------------------------------------
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /build/target/academico-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
