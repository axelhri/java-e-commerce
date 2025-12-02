# Dockerfile Documentation

Ce Dockerfile est utilisé pour construire et exécuter une application Java basée sur Maven et Spring Boot. Il utilise une **multi-stage build** pour optimiser la taille de l'image finale.

**[Dockerfile](/.docker/Dockerfile)**

---

## Étapes de construction

### 1. Stage de build

```dockerfile
FROM maven:3.9.3-eclipse-temurin-17 AS build
```

* Utilise l'image Maven officielle avec JDK 17.
* Le nom `build` est donné à ce stage pour pouvoir copier les artefacts dans le stage final.

```dockerfile
WORKDIR /app
```

* Définit le répertoire de travail à `/app` dans le container.

```dockerfile
COPY pom.xml .
RUN mvn dependency:go-offline -B
```

* Copie uniquement le fichier `pom.xml`.
* Télécharge toutes les dépendances Maven pour les avoir en cache, ce qui accélère les builds ultérieurs.

```dockerfile
COPY src ./src
RUN mvn clean package -DskipTests
```

* Copie le code source dans le conteneur.
* Compile et package l'application en un JAR exécutable, en **sautant les tests** pour accélérer le build.

---

### 2. Stage de production

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
```

* Image légère Alpine avec JDK 17 pour exécuter l'application.
* Réduit la taille finale de l'image par rapport au stage de build complet.

```dockerfile
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
```

* Crée un utilisateur non-root `spring` pour des raisons de sécurité.
* Définit cet utilisateur comme utilisateur actif du conteneur.

```dockerfile
COPY --from=build /app/target/*.jar app.jar
```

* Copie le JAR construit depuis le stage `build` vers le stage final.

```dockerfile
EXPOSE 8080
```

* Indique que l'application écoute sur le port 8080.

```dockerfile
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

* Définit le point d'entrée du conteneur pour exécuter l'application Java.

---

## Résumé

* **Multi-stage build** pour séparer la compilation et l'exécution.
* **Optimisation de l'image** : dépendances Maven en cache, image Alpine finale légère.
* **Sécurité** : exécution en tant qu'utilisateur non-root.
* **Port exposé** : 8080, standard pour Spring Boot.
* **Commande de lancement** : `java -jar /app/app.jar`.
