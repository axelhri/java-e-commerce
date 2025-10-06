#!/bin/bash
set -e

SONARQUBE_URL="http://localhost:9000"
ADMIN_USER="admin"
ADMIN_PASS="admin"
NEW_ADMIN_PASS="SuperMotDePasse12."
NEW_USER="devuser"
NEW_USER_PASS="Password123."
PROJECT_KEY="starter"
PROJECT_NAME="starter"
TOKEN_NAME="starter-token"
ENV_FILE=".env"

# Attendre que SonarQube soit UP
until curl -s -o /dev/null "$SONARQUBE_URL/api/system/status"; do
  echo "⏳ Waiting for SonarQube..."
  sleep 5
done

echo "✅ SonarQube is up"

# Changer mot de passe admin
curl -u $ADMIN_USER:$ADMIN_PASS -X POST "$SONARQUBE_URL/api/users/change_password" \
  -d "login=admin&previousPassword=$ADMIN_PASS&password=$NEW_ADMIN_PASS" || true

# Créer un utilisateur
curl -u admin:$NEW_ADMIN_PASS -X POST "$SONARQUBE_URL/api/users/create" \
  -d "login=$NEW_USER&name=$NEW_USER&password=$NEW_USER_PASS" || true

# Ajouter cet utilisateur aux admins
curl -u admin:$NEW_ADMIN_PASS -X POST "$SONARQUBE_URL/api/user_groups/add_user" \
  -d "name=sonar-administrators&login=$NEW_USER" || true

# Créer un projet starter
curl -u admin:$NEW_ADMIN_PASS -X POST "$SONARQUBE_URL/api/projects/create" \
  -d "project=$PROJECT_KEY&name=$PROJECT_NAME" || true

# Générer un token pour ce projet
TOKEN=$(curl -s -u admin:$NEW_ADMIN_PASS -X POST "$SONARQUBE_URL/api/user_tokens/generate" \
  -d "name=$TOKEN_NAME" | jq -r '.token')

if [ "$TOKEN" != "null" ] && [ -n "$TOKEN" ]; then
  echo "✅ Token généré pour le projet '$PROJECT_NAME'"

  # Ensure newline at end of .env file
  if [ -s "$ENV_FILE" ] && [ "$(tail -c1 "$ENV_FILE")" != "" ]; then
    echo "" >> "$ENV_FILE"
  fi

  # Clean old values
  sed -i "/^SONAR_PROJECT_KEY=/d" "$ENV_FILE"
  sed -i "/^SONAR_TOKEN=/d" "$ENV_FILE"

  # Append to .env
  {
    echo "SONAR_PROJECT_KEY=$PROJECT_KEY"
    echo "SONAR_TOKEN=$TOKEN"
  } >> "$ENV_FILE"

  echo "🔐 Variables ajoutées dans $ENV_FILE"
else
  echo "⚠️ Impossible de générer le token (peut-être déjà créé ?)"
fi