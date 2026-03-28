#!/bin/bash
# Generate self-signed SSL certificate for HTTPS
# Usage: ./setup-ssl.sh [IP_OR_DOMAIN]
# Example: ./setup-ssl.sh 192.168.1.100
#          ./setup-ssl.sh myserver.example.com

set -e

HOST="$1"
if [ -z "$HOST" ]; then
  echo "Usage: ./setup-ssl.sh <IP_OR_DOMAIN>"
  echo "Example: ./setup-ssl.sh 192.168.1.100"
  exit 1
fi
CERT_DIR="$(cd "$(dirname "$0")" && pwd)/certs"

mkdir -p "$CERT_DIR"

echo "Generating SSL certificate for: $HOST"

# Create openssl config
cat > "$CERT_DIR/openssl.cnf" <<EOF
[req]
default_bits = 2048
prompt = no
default_md = sha256
x509_extensions = v3_req
distinguished_name = dn

[dn]
C = CN
ST = Local
L = Local
O = RAGFlow Admin
CN = $HOST

[v3_req]
subjectAltName = @alt_names

[alt_names]
IP.1 = $HOST
DNS.1 = $HOST
DNS.2 = localhost
IP.2 = 127.0.0.1
EOF

# Generate certificate
openssl req -x509 -nodes -days 3650 \
  -newkey rsa:2048 \
  -keyout "$CERT_DIR/nginx.key" \
  -out "$CERT_DIR/nginx.crt" \
  -config "$CERT_DIR/openssl.cnf"

echo ""
echo "========================================="
echo "  SSL certificate generated!"
echo "  Location: $CERT_DIR/"
echo "  Host: $HOST"
echo "  Valid for: 10 years"
echo "========================================="
echo ""
echo "Note: This is a self-signed certificate."
echo "Browsers will show a security warning."
echo "Click 'Advanced' -> 'Proceed' to continue."
