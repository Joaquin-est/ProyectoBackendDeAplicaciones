#!/usr/bin/env bash
set -euo pipefail

# Configuración de Keycloak para Técnica Patito (Pasos 2, 3 y 4 de la guía)
KC_CONTAINER="${KC_CONTAINER:-keycloak-patito}"
REALM="${REALM:-patito}"
CLIENT_ID="${CLIENT_ID:-patito-api}"
CLIENT_SECRET="${CLIENT_SECRET:-patito-secret-1234}"
KC_ADMIN_USER="${KC_ADMIN_USER:-admin}"
KC_ADMIN_PASS="${KC_ADMIN_PASS:-admin}"

kc() {
  docker exec "$KC_CONTAINER" /opt/keycloak/bin/kcadm.sh "$@"
}

echo "==> Esperando a que Keycloak esté disponible en el contenedor '$KC_CONTAINER'..."
until kc config credentials \
  --server http://localhost:8080 \
  --realm master \
  --user "$KC_ADMIN_USER" \
  --password "$KC_ADMIN_PASS" >/dev/null 2>&1; do
  sleep 2
done
echo "Keycloak listo."

# ---------- Paso 2: Crear realm y roles ----------
if kc get "realms/$REALM" >/dev/null 2>&1; then
  echo "==> Realm '$REALM' ya existe."
else
  echo "==> Creando realm '$REALM'..."
  kc create realms -s "realm=$REALM" -s enabled=true -s registrationAllowed=false
fi

echo "==> Habilitando atributos no gestionados (Unmanaged Attributes)..."
kc update "users/profile" -r "$REALM" -s unmanagedAttributePolicy=ENABLED >/dev/null 2>&1 || \
  echo "    Aviso: no se pudo actualizar users/profile por CLI; verificá Realm settings -> Unmanaged Attributes = Enabled en la consola."

for rol_desc in \
  "ADMIN:Administrador del sistema Técnica Patito" \
  "CLIENTE:Cliente que solicita servicios técnicos" \
  "TECNICO:Técnico que atiende solicitudes" \
  "bibliotecario:Rol de prueba de la guía (equivalente a administrador)" \
  "socio:Rol de prueba de la guía (equivalente a cliente)"
do
  ROL="${rol_desc%%:*}"
  DESC="${rol_desc#*:}"
  if kc get "roles/$ROL" -r "$REALM" >/dev/null 2>&1; then
    echo "    Rol '$ROL' ya existe."
  else
    kc create roles -r "$REALM" -s "name=$ROL" -s "description=$DESC"
    echo "    Rol '$ROL' creado."
  fi
done

# ---------- Paso 3: Crear client y protocol mappers ----------
CID=$(kc get clients -r "$REALM" -q "clientId=$CLIENT_ID" --fields id --format csv --noquotes | tr -d '\r')
if [ -z "$CID" ]; then
  echo "==> Creando client '$CLIENT_ID'..."
  kc create clients -r "$REALM" \
    -s "clientId=$CLIENT_ID" \
    -s publicClient=false \
    -s "secret=$CLIENT_SECRET" \
    -s standardFlowEnabled=true \
    -s directAccessGrantsEnabled=true \
    -s serviceAccountsEnabled=true \
    -s 'redirectUris=["http://localhost:41071/*","http://localhost:3000/*"]' \
    -s 'webOrigins=["http://localhost:41071","http://localhost:3000"]'
  CID=$(kc get clients -r "$REALM" -q "clientId=$CLIENT_ID" --fields id --format csv --noquotes | tr -d '\r')
else
  echo "==> Client '$CLIENT_ID' ya existe (id: $CID)."
  kc update "clients/$CID" -r "$REALM" \
    -s publicClient=false \
    -s "secret=$CLIENT_SECRET" \
    -s standardFlowEnabled=true \
    -s directAccessGrantsEnabled=true \
    -s serviceAccountsEnabled=true >/dev/null
fi

echo "==> Asignando permisos de realm-admin al service account de '$CLIENT_ID'..."
kc add-roles -r "$REALM" \
  --uusername "service-account-$CLIENT_ID" \
  --cclientid realm-management \
  --rolename realm-admin >/dev/null 2>&1 || true

MAPPERS_EXISTENTES=$(kc get "clients/$CID/protocol-mappers/models" -r "$REALM" --fields name --format csv --noquotes | tr -d '\r')

if ! echo "$MAPPERS_EXISTENTES" | grep -qx "roles-planos"; then
  echo "==> Creando mapper 'roles-planos'..."
  kc create "clients/$CID/protocol-mappers/models" -r "$REALM" \
    -s name=roles-planos -s protocol=openid-connect \
    -s protocolMapper=oidc-usermodel-realm-role-mapper \
    -s 'config."claim.name"=roles' \
    -s 'config."multivalued"=true' \
    -s 'config."jsonType.label"=String' \
    -s 'config."access.token.claim"=true' \
    -s 'config."id.token.claim"=false' \
    -s 'config."userinfo.token.claim"=false'
fi

if ! echo "$MAPPERS_EXISTENTES" | grep -qx "socio-id"; then
  echo "==> Creando mapper 'socio-id'..."
  kc create "clients/$CID/protocol-mappers/models" -r "$REALM" \
    -s name=socio-id -s protocol=openid-connect \
    -s protocolMapper=oidc-usermodel-attribute-mapper \
    -s 'config."user.attribute"=socio_id' \
    -s 'config."claim.name"=socio_id' \
    -s 'config."jsonType.label"=long' \
    -s 'config."access.token.claim"=true' \
    -s 'config."id.token.claim"=false' \
    -s 'config."userinfo.token.claim"=false'
fi

if ! echo "$MAPPERS_EXISTENTES" | grep -qx "audiencia-api"; then
  echo "==> Creando mapper 'audiencia-api'..."
  kc create "clients/$CID/protocol-mappers/models" -r "$REALM" \
    -s name=audiencia-api -s protocol=openid-connect \
    -s protocolMapper=oidc-audience-mapper \
    -s "config.\"included.client.audience\"=$CLIENT_ID" \
    -s 'config."access.token.claim"=true' \
    -s 'config."id.token.claim"=false'
fi

# ---------- Paso 4: Crear usuarios de prueba ----------
crear_usuario() {
  local username="$1"
  local password="$2"
  local email="$3"
  local first_name="$4"
  local last_name="$5"
  local role="$6"
  local socio_id="${7:-}"

  local uid
  uid=$(kc get users -r "$REALM" -q "username=$username" --fields id --format csv --noquotes | tr -d '\r' | head -n 1)

  if [ -z "$uid" ]; then
    if [ -n "$socio_id" ]; then
      kc create users -r "$REALM" \
        -s "username=$username" -s enabled=true -s emailVerified=true \
        -s "email=$email" -s "firstName=$first_name" -s "lastName=$last_name" \
        -s "attributes.socio_id=$socio_id"
    else
      kc create users -r "$REALM" \
        -s "username=$username" -s enabled=true -s emailVerified=true \
        -s "email=$email" -s "firstName=$first_name" -s "lastName=$last_name"
    fi
    echo "    Usuario '$username' creado."
  else
    if [ -n "$socio_id" ]; then
      kc update "users/$uid" -r "$REALM" -s "attributes.socio_id=$socio_id" >/dev/null
    fi
    echo "    Usuario '$username' ya existe."
  fi

  kc set-password -r "$REALM" --username "$username" --new-password "$password"
  kc add-roles -r "$REALM" --uusername "$username" --rolename "$role" >/dev/null 2>&1 || true
}

echo "==> Creando/actualizando usuarios de prueba..."
# Usuarios de Técnica Patito
crear_usuario "superadmin" "admin123" "admin@patito.com" "Super" "Admin" "ADMIN"
crear_usuario "cliente1" "1234" "cliente1@patito.com" "Carlos" "Garcia" "CLIENTE" "1"
crear_usuario "cliente2" "1234" "cliente2@patito.com" "Maria" "Rodriguez" "CLIENTE" "2"
crear_usuario "88887777" "88887777" "tecnico1@patito.com" "Juan" "Perez" "TECNICO"

# Usuarios de la guía (para pruebas con roles bibliotecario/socio)
crear_usuario "laura.biblio" "Laura123!" "laura@example.com" "Laura" "Gomez" "bibliotecario"
crear_usuario "ana.socia" "Ana12345!" "ana@example.com" "Ana" "Diaz" "socio" "1"
crear_usuario "bruno.socio" "Bruno123!" "bruno@example.com" "Bruno" "Paz" "socio" "2"

echo ""
echo "==> Configuración de Keycloak completada para realm '$REALM' y client '$CLIENT_ID'."
