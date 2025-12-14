#!/bin/bash

# Create the keys directory if it doesn't exist
mkdir -p authentication-service/src/main/resources/keys

# Generate a 2048-bit RSA private key
openssl genpkey -algorithm RSA -out authentication-service/src/main/resources/keys/private_key.pem -pkeyopt rsa_keygen_bits:2048

# Extract the public key from the private key
openssl rsa -pubout -in authentication-service/src/main/resources/keys/private_key.pem -out authentication-service/src/main/resources/keys/public_key.pem

echo "Keys generated successfully in authentication-service/src/main/resources/keys/"
