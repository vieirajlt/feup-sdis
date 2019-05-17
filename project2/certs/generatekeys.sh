#!/bin/bash

# Generate client public/private key pair into private keystore
echo Generating client public private key pair
keytool -genkey -alias clientprivate -keystore client.private -storetype JKS -keyalg rsa -dname "CN=Your Name, OU=Your Organizational Unit, O=Your Organization, L=Your City, S=Your State, C=Your Country" -storepass clientpw -keypass clientpw

# Generate server public/private key pair
echo Generating server public private key pair
keytool -genkey -alias serverprivate -keystore server.private -storetype JKS -keyalg rsa -dname "CN=Your Name, OU=Your Organizational Unit, O=Your Organization, L=Your City, S=Your State, C=Your Country" -storepass serverpw -keypass serverpw

# Export client public key and import it into public keystore
echo Generating client public key file
keytool -export -alias clientprivate -keystore client.private -file temp.key -storepass clientpw
keytool -import -noprompt -alias clientpublic -keystore client.public -file temp.key -storepass public
rm -f temp.key

# Export server public key and import it into public keystore
echo Generating server public key file
keytool -export -alias serverprivate -keystore server.private -file temp.key -storepass serverpw
keytool -import -noprompt -alias serverpublic -keystore server.public -file temp.key -storepass public
rm -f temp.key
