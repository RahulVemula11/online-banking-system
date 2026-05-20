#!/bin/sh
set -e

# Render gives DATABASE_URL like:
#   postgresql://user:password@host:5432/dbname
# Spring needs it split into a jdbc URL + separate username/password.
if [ -n "$DATABASE_URL" ] && [ -z "$JDBC_DATABASE_URL" ]; then
  no_scheme=$(echo "$DATABASE_URL" | sed -e 's,^[a-zA-Z]*://,,')
  creds=$(echo "$no_scheme" | cut -d@ -f1)
  hostpart=$(echo "$no_scheme" | cut -d@ -f2)
  export DB_USERNAME=$(echo "$creds" | cut -d: -f1)
  export DB_PASSWORD=$(echo "$creds" | cut -d: -f2)
  export JDBC_DATABASE_URL="jdbc:postgresql://${hostpart}"
  echo "Configured JDBC_DATABASE_URL for host: $(echo "$hostpart" | cut -d/ -f1)"
fi

exec java -jar app.jar
