# Installation

## Container setup

```bash
make dev
```

## Start spring boot (local development)

```bash
make run
```

---

# SonarQube analysis

## Create project

```bash
make sonar-init
```

**login :**
- **username :** `devuser`
- **password :** `Password123.`

## Start analysis

```bash
make sonar
```

---

# Services

- Springboot app : http://localhost:8080/
- PG Web : http://localhost:8081/
- SonarQube : http://localhost:9000/

---

# Github Action CI

**Github secrets variables** :
- `SONAR_ORGANIZATION` : Organization key
- `SONAR_PROJECT_KEY` : Project key
- `SONAR_HOST_URL` : Sonar Cloud host (https://sonarcloud.io)
- `SONAR_TOKEN` : Project token

_*All of these informations are available in your **Sonar Cloud** project information tab*_