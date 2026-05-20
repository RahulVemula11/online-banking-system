#!/bin/sh
set -e

# If the platform gave us a postgres://user:pass@host:port/db style URL and we
# don't already have JDBC vars, parse it into the pieces Spring wants.
if [ -n "$DATABASE_URL" ] && [ -z "$JDBC_DATABASE_URL" ]; then
  # strip scheme
  no_scheme=$(echo "$DATABASE_URL" | sed -e 's,^[a-zA-Z]*://,,')
  creds=$(echo "$no_scheme" | cut -d@ -f1)
  hostpart=$(echo "$no_scheme" | cut -d@ -f2)
  export DB_USERNAME=$(echo "$creds" | cut -d: -f1)
  export DB_PASSWORD=$(echo "$creds" | cut -d: -f2)
  export JDBC_DATABASE_URL="jdbc:postgresql://${hostpart}"
fi

exec java -jar app.jar
