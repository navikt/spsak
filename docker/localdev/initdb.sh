#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE fpsak_unit;
	CREATE USER fpsak_unit PASSWORD '''${POSTGRES_PASSWORD_FPSAK_UNIT}''';
	GRANT ALL PRIVILEGES ON DATABASE fpsak_unit TO fpsak_unit;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE felles_behandlingsprosess_unit;
	CREATE USER felles_behandlingsprosess_unit PASSWORD '''${POSTGRES_PASSWORD_FELLES_BEHANDLINGSPROSESS_UNIT}''';
	GRANT ALL PRIVILEGES ON DATABASE felles_behandlingsprosess_unit TO felles_behandlingsprosess_unit;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE fpsak;
	CREATE USER fpsak PASSWORD '''${POSTGRES_PASSWORD_FPSAK}''';
	GRANT ALL PRIVILEGES ON DATABASE fpsak TO fpsak;
EOSQL

