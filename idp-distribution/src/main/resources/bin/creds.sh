#! /bin/sh

# This is a rudimentary script to generate credentials for the IdP.
# Run from within the idp.home directory as bin/creds.sh.

HOSTNAME=idp.example.org

URI_ALT_NAME=https://idp.example.org/idp/shibboleth

CLASS=net.shibboleth.utilities.java.support.security.SelfSignedCertificateGenerator

java -cp "bin/lib/*:war/WEB-INF/lib/*" $CLASS --hostname $HOSTNAME --keyfile creds/idp-signing.key --certfile creds/idp-signing.crt --uriAltName $URI_ALT_NAME

java -cp "bin/lib/*:war/WEB-INF/lib/*" $CLASS --hostname $HOSTNAME --keyfile creds/idp-encryption.key --certfile creds/idp-encryption.crt --uriAltName $URI_ALT_NAME

java -cp "bin/lib/*:war/WEB-INF/lib/*" $CLASS --hostname $HOSTNAME --storefile creds/idp-tls.p12 --storepass changeit --uriAltName $URI_ALT_NAME

CLASS=net.shibboleth.utilities.java.support.security.BasicKeystoreKeyStrategyTool

java -cp "bin/lib/*:war/WEB-INF/lib/*" $CLASS --storefile creds/secret.jks --versionfile creds/secret.kver --alias secret --storepass password
