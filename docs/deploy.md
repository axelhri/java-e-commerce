# Documentation GitHub Actions - Continuous Delivery & Deployment

Ce workflow GitHub Actions gère la **livraison continue et le déploiement** d'une application Spring Boot sur un serveur VPS à partir des builds CI réussis.

**[deploy.yml](../.github/workflows/deploy.yml)**

---

## Déclencheurs

```yaml
on:
  workflow_run:
    workflows: ["CI - Spring Boot"]
    branches: [main]
    types: [completed]
```

* S'exécute automatiquement après l'achèvement du workflow **CI - Spring Boot**.
* Limité aux commits sur la branche `main`.
* Ne s'exécute que lorsque le workflow CI a été complété.

---

## Jobs

### Deploy

```yaml
jobs:
  deploy:
    if: ${{ github.event.workflow_run.conclusion == 'success' }}
    runs-on: ubuntu-latest
```

* Le job de déploiement ne démarre que si le workflow CI s'est terminé avec succès.
* Utilise un runner Ubuntu récent.

#### Étapes

1. **Checkout du repository**

```yaml
- name: Checkout repository
  uses: actions/checkout@v4
```

* Récupère le code du dépôt.

2. **Rendre mvnw exécutable**

```yaml
- name: Make mvnw executable
  run: chmod +x mvnw
```

* Permet l'exécution de Maven Wrapper.

3. **Extraction de la version du projet**

```yaml
- name: Extract project version
  id: extract_version
  run: |
    VERSION=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
    echo "VERSION=$VERSION"
    echo "VERSION=$VERSION" >> $GITHUB_OUTPUT
```

* Récupère la version du projet définie dans le `pom.xml`.
* Permet de taguer l'image Docker avec la version exacte.

4. **Configuration de Docker Buildx**

```yaml
- name: Set up Docker Buildx
  uses: docker/setup-buildx-action@v3
```

* Prépare l'environnement pour la construction multi-plateformes et le cache.

5. **Connexion à Docker Hub**

```yaml
- name: Login to Docker Hub
  uses: docker/login-action@v3
  with:
    username: ${{ secrets.DOCKER_USERNAME }}
    password: ${{ secrets.DOCKER_PASSWORD }}
```

* Authentification pour pousser les images Docker.

6. **Build et push de l'image Docker**

```yaml
- name: Build and push docker image
  uses: docker/build-push-action@v5
  with:
    context: .
    file: ./.docker/Dockerfile
    push: true
    tags: |
      ${{ secrets.DOCKER_USERNAME }}/${{ secrets.APP_NAME }}:latest
      ${{ secrets.DOCKER_USERNAME }}/${{ secrets.APP_NAME }}:${{ steps.extract_version.outputs.VERSION }}
    cache-from: type=gha
    cache-to: type=gha, mode=mas
```

* Construit et pousse l'image Docker sur Docker Hub.
* Tag `latest` et tag correspondant à la version du projet.
* Utilise le cache pour accélérer la construction.

7. **Déploiement sur le VPS**

```yaml
- name: Deploy to VPS
  uses: appleboy/ssh-action@v1.0.0
  with:
    host: ${{ secrets.VPS_HOST }}
    username: ${{ secrets.VPS_USERNAME }}
    key: ${{ secrets.VPS_SSH_KEY }}
    port: ${{ secrets.VPS_PORT }}
    passphrase: ${{ secrets.VPS_PASSPHRASE }}
    script: |
      cd /opt/${{ secrets.APP_NAME }} || sudo mkdir -p /opt/${{ secrets.APP_NAME }} && cd /opt/${{ secrets.APP_NAME }}
      
      cat > .env << EOF
      APP_NAME=${{ secrets.APP_NAME }}
      DOCKER_USERNAME=${{ secrets.DOCKER_USERNAME }}
      POSTGRES_USER=${{ secrets.POSTGRES_USER }}
      POSTGRES_PASSWORD=${{ secrets.POSTGRES_PASSWORD }}
      POSTGRES_DB=${{ secrets.POSTGRES_DB }}
      REDIS_PASSWORD=${{ secrets.REDIS_PASSWORD }}
      EOF
      
      rm compose.prod.yml
      
      if [ ! -f compose.prod.yml ]; then
        curl -H "Authorization: Bearer ${{ secrets.GH_TOKEN }}" -o compose.prod.yml https://raw.githubusercontent.com/${{ github.repository }}/main/compose.prod.yml
      fi
      
      docker compose -f compose.prod.yml --env-file .env pull
      docker compose -f compose.prod.yml --env-file .env down
      docker compose -f compose.prod.yml --env-file .env up -d
      docker image prune -f
```

* Se connecte en SSH au VPS.
* Prépare le répertoire de déploiement et le fichier `.env`.
* Télécharge `compose.prod.yml` depuis le repository si nécessaire.
* Met à jour les conteneurs avec la dernière image Docker.
* Supprime les images Docker inutilisées pour libérer de l'espace.

---

## Résumé

* Workflow déclenché automatiquement après un CI réussi sur `develop`.
* Build et push de l'image Docker avec tag version et latest.
* Déploiement sur VPS via SSH et Docker Compose.
* Gestion de l'environnement via `.env`.
* Nettoyage des images Docker inutilisées pour maintenir le serveur propre.
* Garantit un déploiement fiable et reproductible pour la branche de développement.
