# Documentation GitHub Actions - Continuous Integration (CI) Spring Boot

Ce fichier GitHub Actions configure un workflow de **CI**, incluant compilation, tests, analyses de code, analyse SonarQube et packaging.

**[CI](../.github/workflows/ci.yml)**

---

## Déclencheurs

```yaml
on:
  push:
    branches: [ "main", "develop" ]
  pull_request:
    branches: [ "main", "develop" ]
```

* Le workflow s'exécute sur les branches `main` et `develop`.
* Déclenché à la fois sur les push et sur les pull requests.

---

## Jobs

### 1. Build

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    if: github.event.head_commit.message != 'Initial commit'
```

* Le job `build` s'exécute sur un runner Ubuntu récent.
* Ignore le commit initial pour éviter des builds inutiles.

#### Étapes

1. **Checkout du repository**

```yaml
- name: Checkout repository
  uses: actions/checkout@v4
  with:
    fetch-depth: 0
```

* Récupère l'intégralité du repository pour permettre toutes les opérations Git.

2. **Configuration du JDK 17**

```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: 17
```

* Installe le JDK 17 Temurin pour compiler et tester l'application.

3. **Vérification du formatage du code**

```yaml
- name: Check code formatting
  run: mvn spotless:check
```

* Utilise **Spotless** pour vérifier que le code respecte les règles de formatage définies.

4. **Analyse SonarQube**

```yaml
- name: SonarQube Analysis
  if: github.ref != 'refs/heads/develop'
  run: |
    mvn clean verify sonar:sonar \
      -Dsonar.organization=${{ secrets.SONAR_ORGANIZATION  }} \
      -Dsonar.projectKey=${{ secrets.SONAR_PROJECT_KEY }} \
      -Dsonar.host.url=${{ secrets.SONAR_HOST_URL }} \
      -Dsonar.token=${{ secrets.SONAR_TOKEN  }} \
      -Dsonar.sources=src/main/java \
      -Dsonar.tests=src/test/java \
      -Dsonar.java.binaries=target/classes \
      -Dsonar.coverage.exclusions=** \
      -Dsonar.qualitygate.wait=true
```

* Analyse de la qualité du code avec SonarQube.
* S'exécute uniquement pour les branches autres que `develop`.
* Utilisation des secrets GitHub pour les credentials.

5. **Exécution des tests**

```yaml
- name: Run Tests
  if: success()
  run: mvn clean test
```

* Lance tous les tests unitaires et d'intégrations Maven si les étapes précédentes ont réussi.

6. **Packaging du JAR**

```yaml
- name: Package JAR
  if: success()
  run: mvn clean package -DskipTests
```

* Crée le JAR exécutable, en sautant les tests pour accélérer.

7. **Upload du JAR**

```yaml
- name: Upload JAR Artifact
  if: success() && github.ref == 'refs/heads/main'
  uses: actions/upload-artifact@v4
  with:
    name: springboot-starter-app
    path: target/*.jar
```

* Téléverse l'artefact JAR uniquement depuis la branche `main`.

---

## Résumé

* Workflow CI pour Spring Boot avec Maven.
* Contrôle de code, tests, packaging et analyse SonarQube intégrés.
* Artefact final disponible sur la branche `main`.
* Utilisation de secrets pour les paramètres sensibles (SonarQube).
* Optimisé pour éviter les builds inutiles sur le commit initial et certaines branches.
