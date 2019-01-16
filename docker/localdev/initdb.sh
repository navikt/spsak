#!/bin/bash
set -e

export POSTGRES_USER=${POSTGRES_USER:-postgres}

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE spsak_unit;
	CREATE USER spsak_unit PASSWORD '${POSTGRES_PASSWORD_FPSAK_UNIT:-spsak_unit}';
	GRANT ALL PRIVILEGES ON DATABASE spsak_unit TO spsak_unit;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE felles_behandlingsprosess_unit;
	CREATE USER felles_behandlingsprosess_unit PASSWORD '${POSTGRES_PASSWORD_FELLES_BEHANDLINGSPROSESS_UNIT:-felles_behandlingsprosess_unit}';
	GRANT ALL PRIVILEGES ON DATABASE felles_behandlingsprosess_unit TO felles_behandlingsprosess_unit;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE spsak;
	CREATE USER spsak PASSWORD '${POSTGRES_PASSWORD_FPSAK:-spsak}';
	GRANT ALL PRIVILEGES ON DATABASE spsak TO spsak;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE vl_fordeling_unit;
	CREATE USER vl_fordeling_unit PASSWORD '${POSTGRES_PASSWORD_VL_FORDELING_UNIT:-vl_fordeling_unit}';
	GRANT ALL PRIVILEGES ON DATABASE vl_fordeling_unit TO vl_fordeling_unit;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE fpfordel;
	CREATE USER fpfordel PASSWORD '${POSTGRES_PASSWORD_FPFORDEL:-fpfordel}';
	GRANT ALL PRIVILEGES ON DATABASE fpfordel TO fpfordel;
EOSQL
