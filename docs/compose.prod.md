# Documentation compose.prod.yml

Ce fichier `compose.prod.yml` est un fichier Docker Compose pour déployer l'application en **production**, incluant les services **PostgreSQL**, **Redis**, et l'application Spring Boot.

**[compose.prod.yml](../compose.prod.yml)**

---

## Services

### 1. PostgreSQL (db)

```yaml
db:
  image: postgres:17
  container_name: postgres_db_prod
  environment:
    POSTGRES_USER: ${POSTGRES_USER}
    POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    POSTGRES_DB: ${POSTGRES_DB}
  volumes:
    - postgres_data:/var/lib/postgresql/data
  healthcheck:
    test: [ "CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}" ]
    interval: 10s
    timeout: 5s
    retries: 5
  restart: unless-stopped
```

* Utilise l'image PostgreSQL version 17.
* Définit le nom du conteneur en production : `postgres_db_prod`.
* Les variables d'environnement sont définies via `.env`.
* Persiste les données avec le volume `postgres_data`.
* Healthcheck pour s'assurer que la base est prête.
* Redémarre automatiquement sauf si le conteneur est arrêté manuellement.

### 2. Redis (redis)

```yaml
redis:
  image: redis:7.4.2
  container_name: redis_prod
  command: [ "redis-server", "--appendonly", "yes", "--requirepass", "${REDIS_PASSWORD}" ]
  environment:
    - ${REDIS_PASSWORD}
  volumes:
    - redis_data_prod:/data
  restart: unless-stopped
```

* Redis version 7.4.2.
* Activation du mode append-only pour la persistance.
* Protection par mot de passe via variable d'environnement.
* Persistance des données avec `redis_data_prod`.
* Redémarrage automatique.

### 3. Application (app)

```yaml
app:
  image: ${DOCKER_USERNAME}/${APP_NAME}:latest
  container_name: ${APP_NAME}
  depends_on:
    db:
      condition: service_healthy
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/${POSTGRES_DB}
    SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER}
    SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
    SPRING_PROFILES_ACTIVE: prod
    JAVA_OPTS: -Xms256m -Xmx512m
  env_file:
    - .env
  ports:
    - "8080:8080"
  restart: unless-stopped
```

* Image Docker construite pour l'application.
* Dépend de la base de données (`db`) et attend que le service soit sain.
* Variables d'environnement pour configurer Spring Boot et la connexion à PostgreSQL.
* Utilise un fichier `.env` pour charger d'autres variables.
* Expose le port 8080 pour accéder à l'application.
* Redémarrage automatique.

---

## Volumes

```yaml
volumes:
  postgres_data:
  redis_data_prod:
```

* `postgres_data` : stocke les données PostgreSQL.
* `redis_data_prod` : stocke les données Redis.

---

## Résumé

* **Base de données** : PostgreSQL 17 avec persistance et healthcheck.
* **Cache** : Redis avec persistance et mot de passe.
* **Application** : Spring Boot en profile `prod`.
* **Sécurité** : utilisateur et mot de passe via `.env`.
* **Redémarrage automatique** pour tous les services.
* **Ports exposés** : 8080 pour l'application.
