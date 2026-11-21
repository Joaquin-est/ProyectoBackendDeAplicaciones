#!/usr/bin/env bash
set -euo pipefail

# Batería de pruebas de Keycloak + API Gateway (Pasos 5 y 10 de la guía)
GATEWAY_URL="${GATEWAY_URL:-http://localhost:41071}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:41090}"
REALM="${REALM:-patito}"
CLIENT_ID="${CLIENT_ID:-patito-api}"
CLIENT_SECRET="${CLIENT_SECRET:-patito-secret-1234}"
KC_CONTAINER="${KC_CONTAINER:-keycloak-patito}"

token() {
  curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
    -d grant_type=password \
    -d "client_id=$CLIENT_ID" \
    -d "client_secret=$CLIENT_SECRET" \
    -d "username=$1" \
    -d "password=$2" | jq -r .access_token
}

claims() {
  echo "$1" | jq -R 'split(".")[1] | @base64d | fromjson'
}

FALLOS=0
probar() { # uso: probar METODO RUTA ESPERADO [TOKEN]
  local metodo="$1"
  local ruta="$2"
  local esperado="$3"
  local tok="${4:-}"
  local codigo

  if [ -n "$tok" ]; then
    codigo=$(curl -s -o /dev/null -w "%{http_code}" -X "$metodo" \
      -H "Authorization: Bearer $tok" "$GATEWAY_URL$ruta")
  else
    codigo=$(curl -s -o /dev/null -w "%{http_code}" -X "$metodo" \
      "$GATEWAY_URL$ruta")
  fi

  if [ "$codigo" = "$esperado" ]; then
    echo "OK   [$codigo == $esperado] $metodo $ruta"
  else
    echo "FAIL [$codigo != $esperado] $metodo $ruta"
    FALLOS=$((FALLOS + 1))
  fi
}

echo "==> Obteniendo tokens de prueba desde $KEYCLOAK_URL/realms/$REALM..."
T_BIBLIO=$(token laura.biblio 'Laura123!')
T_ANA=$(token ana.socia 'Ana12345!')
T_BRUNO=$(token bruno.socio 'Bruno123!')
T_ADMIN=$(token superadmin 'admin123')
T_CLIENTE1=$(token cliente1 '1234')
T_CLIENTE2=$(token cliente2 '1234')

echo "==> Claims de ana.socia:"
claims "$T_ANA" | jq '{iss, aud, roles, socio_id}'

echo ""
echo "==> 10.1 Pruebas de acceso (guía + Técnica Patito)"
# --- Sin token / token falso ---
probar GET /publico/ping 200
probar GET /libros 401
probar GET /libros 401 "token.falso.invalido"

# --- Laura (bibliotecaria) ---
probar GET /libros 200 "$T_BIBLIO"
probar POST /libros 201 "$T_BIBLIO"
probar GET /prestamos/socio/1 200 "$T_BIBLIO"
probar GET /prestamos/socio/2 200 "$T_BIBLIO"
probar GET /interno/ping 403 "$T_BIBLIO"
probar GET /ruta-inventada 403 "$T_BIBLIO"

# --- Ana (socia, socio_id=1) ---
probar GET /libros 200 "$T_ANA"
probar POST /libros 403 "$T_ANA"
probar GET /prestamos/socio/1 200 "$T_ANA"
probar GET /prestamos/socio/2 403 "$T_ANA"
probar GET /socios 403 "$T_ANA"

# --- Bruno (socio, socio_id=2) ---
probar GET /prestamos/socio/2 200 "$T_BRUNO"
probar GET /prestamos/socio/1 403 "$T_BRUNO"

# --- Técnica Patito: ADMIN y CLIENTE ---
probar GET /api/yo 200 "$T_CLIENTE1"
probar GET /api/clientes 200 "$T_ADMIN"
probar GET /api/clientes 403 "$T_CLIENTE1"
probar GET /api/clientes/1 200 "$T_CLIENTE1"
probar GET /api/clientes/2 403 "$T_CLIENTE1"
probar GET /api/clientes/2 200 "$T_CLIENTE2"
probar GET /api/clientes/1 403 "$T_CLIENTE2"
probar GET /api/solicitudes/cliente/1 200 "$T_CLIENTE1"
probar GET /api/solicitudes/cliente/2 403 "$T_CLIENTE1"
probar GET /api/solicitudes/cliente/2 200 "$T_ADMIN"

echo ""
echo "==> Inspeccionando respuesta /yo de Ana:"
curl -s "$GATEWAY_URL/yo" -H "Authorization: Bearer $T_ANA" | jq .

echo ""
echo "==> 10.2 a) Token de otro realm (master) -> debe dar 401"
T_MASTER=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -d grant_type=password -d client_id=admin-cli -d username=admin -d password=admin | jq -r .access_token)
probar GET /libros 401 "$T_MASTER"

echo ""
echo "==> 10.2 b) Token del mismo realm pero de otro cliente (otro-cliente) -> debe dar 401"
if docker exec "$KC_CONTAINER" /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 --realm master --user admin --password admin >/dev/null 2>&1; then
  if ! docker exec "$KC_CONTAINER" /opt/keycloak/bin/kcadm.sh get clients -r "$REALM" -q clientId=otro-cliente --fields id --format csv --noquotes | grep -q .; then
    docker exec "$KC_CONTAINER" /opt/keycloak/bin/kcadm.sh create clients -r "$REALM" \
      -s clientId=otro-cliente -s publicClient=true -s directAccessGrantsEnabled=true >/dev/null
  fi
  T_OTRO=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
    -d grant_type=password -d client_id=otro-cliente \
    -d username=ana.socia -d password='Ana12345!' | jq -r .access_token)
  probar GET /libros 401 "$T_OTRO"
else
  echo "SKIP: No se pudo crear 'otro-cliente' vía docker exec."
fi

if [ "${1:-}" = "--test-expired" ]; then
  echo ""
  echo "==> 10.2 c) Token vencido (espera 130s por tolerancia de reloj de Spring)..."
  docker exec "$KC_CONTAINER" /opt/keycloak/bin/kcadm.sh update "realms/$REALM" -s accessTokenLifespan=60
  T_CORTO=$(token ana.socia 'Ana12345!')
  probar GET /libros 200 "$T_CORTO"
  sleep 130
  probar GET /libros 401 "$T_CORTO"
  docker exec "$KC_CONTAINER" /opt/keycloak/bin/kcadm.sh update "realms/$REALM" -s accessTokenLifespan=300
fi

echo ""
if [ "$FALLOS" -eq 0 ]; then
  echo "Todas las pruebas pasaron correctamente."
else
  echo "Se detectaron $FALLOS fallos."
  exit 1
fi
