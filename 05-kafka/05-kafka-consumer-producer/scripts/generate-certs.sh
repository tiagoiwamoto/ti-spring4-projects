#!/usr/bin/env bash
set -euo pipefail

# Gera CA, keystore e truststore (PKCS12) com validade até o ano 2999
# Uso: ./scripts/generate-certs.sh

OUT_DIR="$(dirname "$0")/../src/main/resources/certs"
mkdir -p "$OUT_DIR"
cd "$OUT_DIR"

KS_PASS="changeit"
TS_PASS="changeit"
CA_KEY="ca.key"
CA_CERT="ca.crt"
CA_SERIAL="ca.srl"
BROKER_KEY_ALIAS="kafka-broker"
CLIENT_KEY_ALIAS="kafka-client"
BROKER_KEYSTORE="kafka.keystore.p12"
BROKER_TRUSTSTORE="kafka.truststore.p12"
CLIENT_KEYSTORE="kafka-client.keystore.p12"
CLIENT_TRUSTSTORE="kafka-client.truststore.p12"
SCHEMA_KEYSTORE="schema-registry.keystore.p12"
SCHEMA_TRUSTSTORE="schema-registry.truststore.p12"

# calcula dias até 2999-12-31
TARGET_DATE="2999-12-31"
if date -d "$TARGET_DATE" >/dev/null 2>&1; then
  DAYS=$(( ( $(date -d "$TARGET_DATE" +%s) - $(date +%s) ) / 86400 ))
else
  # fallback: usar valor grande (aprox para cobrir até 2999)
  DAYS=356000
fi

echo "Gerando certificados com validade de $DAYS dias (até $TARGET_DATE) em: $OUT_DIR"

# Remove arquivos antigos (cautela)
rm -f "$CA_KEY" "$CA_CERT" "$CA_SERIAL" \
      "$BROKER_KEYSTORE" "$BROKER_TRUSTSTORE" "$CLIENT_KEYSTORE" "$CLIENT_TRUSTSTORE" \
      "$SCHEMA_KEYSTORE" "$SCHEMA_TRUSTSTORE" *.csr *.crt

# 1) CA
openssl genpkey -algorithm RSA -out "$CA_KEY" -pkeyopt rsa_keygen_bits:4096
openssl req -x509 -new -nodes -key "$CA_KEY" -sha256 -days "$DAYS" -subj "/CN=LocalKafkaCA" -out "$CA_CERT"

# 2) Broker keystore (PKCS12)
keytool -genkeypair -alias "$BROKER_KEY_ALIAS" -keyalg RSA -keysize 4096 \
  -dname "CN=kafka" -validity "$DAYS" \
  -keystore "$BROKER_KEYSTORE" -storetype PKCS12 -storepass "$KS_PASS" -keypass "$KS_PASS"

keytool -certreq -alias "$BROKER_KEY_ALIAS" -file broker.csr -keystore "$BROKER_KEYSTORE" -storetype PKCS12 -storepass "$KS_PASS"

openssl x509 -req -in broker.csr -CA "$CA_CERT" -CAkey "$CA_KEY" -CAcreateserial -out broker.crt -days "$DAYS" -sha256

# Import CA then broker cert into broker keystore
keytool -importcert -alias caroot -file "$CA_CERT" -keystore "$BROKER_KEYSTORE" -storetype PKCS12 -storepass "$KS_PASS" -noprompt
keytool -importcert -alias "$BROKER_KEY_ALIAS" -file broker.crt -keystore "$BROKER_KEYSTORE" -storetype PKCS12 -storepass "$KS_PASS" -noprompt

# 3) Broker truststore (contains CA)
keytool -importcert -alias caroot -file "$CA_CERT" -keystore "$BROKER_TRUSTSTORE" -storetype PKCS12 -storepass "$TS_PASS" -noprompt

# 4) Client keystore & truststore (optional, useful for mutual TLS tests)
keytool -genkeypair -alias "$CLIENT_KEY_ALIAS" -keyalg RSA -keysize 4096 \
  -dname "CN=kafka-client" -validity "$DAYS" \
  -keystore "$CLIENT_KEYSTORE" -storetype PKCS12 -storepass "$KS_PASS" -keypass "$KS_PASS"

keytool -certreq -alias "$CLIENT_KEY_ALIAS" -file client.csr -keystore "$CLIENT_KEYSTORE" -storetype PKCS12 -storepass "$KS_PASS"
openssl x509 -req -in client.csr -CA "$CA_CERT" -CAkey "$CA_KEY" -CAcreateserial -out client.crt -days "$DAYS" -sha256
keytool -importcert -alias caroot -file "$CA_CERT" -keystore "$CLIENT_KEYSTORE" -storetype PKCS12 -storepass "$KS_PASS" -noprompt
keytool -importcert -alias "$CLIENT_KEY_ALIAS" -file client.crt -keystore "$CLIENT_KEYSTORE" -storetype PKCS12 -storepass "$KS_PASS" -noprompt

# Create client truststore containing CA (for clients that only verify the broker)
keytool -importcert -alias caroot -file "$CA_CERT" -keystore "$CLIENT_TRUSTSTORE" -storetype PKCS12 -storepass "$TS_PASS" -noprompt

# 5) Schema Registry keystore/truststore (use same broker cert or generate separate)
# For simplicity, we create a separate keystore for Schema Registry
keytool -genkeypair -alias schema-registry -keyalg RSA -keysize 4096 -dname "CN=schema-registry" -validity "$DAYS" \
  -keystore "$SCHEMA_KEYSTORE" -storetype PKCS12 -storepass "$KS_PASS" -keypass "$KS_PASS"

keytool -certreq -alias schema-registry -file schema-registry.csr -keystore "$SCHEMA_KEYSTORE" -storetype PKCS12 -storepass "$KS_PASS"
openssl x509 -req -in schema-registry.csr -CA "$CA_CERT" -CAkey "$CA_KEY" -CAcreateserial -out schema-registry.crt -days "$DAYS" -sha256
keytool -importcert -alias caroot -file "$CA_CERT" -keystore "$SCHEMA_KEYSTORE" -storetype PKCS12 -storepass "$KS_PASS" -noprompt
keytool -importcert -alias schema-registry -file schema-registry.crt -keystore "$SCHEMA_KEYSTORE" -storetype PKCS12 -storepass "$KS_PASS" -noprompt
keytool -importcert -alias caroot -file "$CA_CERT" -keystore "$SCHEMA_TRUSTSTORE" -storetype PKCS12 -storepass "$TS_PASS" -noprompt

# 6) Create convenience client config files (paths relative for local CLI tests)
cat > client-ssl.properties <<EOF
security.protocol=SSL
ssl.truststore.location=$(pwd)/$CLIENT_TRUSTSTORE
ssl.truststore.password=$TS_PASS
ssl.truststore.type=PKCS12
ssl.keystore.location=$(pwd)/$CLIENT_KEYSTORE
ssl.keystore.password=$KS_PASS
ssl.keystore.type=PKCS12
ssl.key.password=$KS_PASS
ssl.endpoint.identification.algorithm=
EOF

cat > broker-ssl.properties <<EOF
ssl.keystore.location=/etc/kafka/secrets/$BROKER_KEYSTORE
ssl.keystore.password=$KS_PASS
ssl.keystore.type=PKCS12
ssl.key.password=$KS_PASS
ssl.truststore.location=/etc/kafka/secrets/$BROKER_TRUSTSTORE
ssl.truststore.password=$TS_PASS
ssl.truststore.type=PKCS12
ssl.client.auth=none
ssl.endpoint.identification.algorithm=
EOF

cat > schema-registry-ssl.properties <<EOF
kafkastore.bootstrap.servers=SSL://kafka:9093
kafkastore.security.protocol=SSL
kafkastore.ssl.keystore.location=/etc/schema-registry/secrets/$SCHEMA_KEYSTORE
kafkastore.ssl.keystore.password=$KS_PASS
kafkastore.ssl.keystore.type=PKCS12
kafkastore.ssl.truststore.location=/etc/schema-registry/secrets/$SCHEMA_TRUSTSTORE
kafkastore.ssl.truststore.password=$TS_PASS
kafkastore.ssl.truststore.type=PKCS12
EOF

# List generated artifacts
echo "Arquivos gerados em: $OUT_DIR"
ls -l

# Summário de verificação
echo "Verificando keystore (broker):"
keytool -list -v -keystore "$BROKER_KEYSTORE" -storetype PKCS12 -storepass "$KS_PASS" | grep -A2 "Alias name"

echo "Pronto. Se for necessário, ajuste senhas e paths antes de subir o docker-compose."

