-- ##################################################
-- ### Opplegg for enhetstester (lokal + jenkins) ###
-- ##################################################
/*DECLARE userexists INTEGER;
BEGIN
  SELECT count(*)
  INTO userexists
  FROM SYS.ALL_USERS
  WHERE USERNAME = upper('${felles_behandlingsprosess_schema_unit}');
  IF (userexists = 0)
  THEN
    EXECUTE IMMEDIATE ('CREATE USER ${felles_behandlingsprosess_schema_unit} IDENTIFIED BY ${felles_behandlingsprosess_schema_unit}');
  END IF;
END;
/

GRANT CONNECT, RESOURCE, CREATE JOB, CREATE TABLE, CREATE SYNONYM, CREATE VIEW, CREATE MATERIALIZED VIEW TO ${felles_behandlingsprosess_schema_unit};
*/
DO $$
DECLARE
  userexists integer;
BEGIN
    SELECT COUNT(*)
    INTO userexists
    FROM pg_roles
    WHERE rolname = lower('${felles_behandlingsprosess_schema_unit}');
    IF (userexists = 0)
    THEN
      CREATE USER ${felles_behandlingsprosess_schema_unit} PASSWORD '${felles_behandlingsprosess_schema_unit}';
    END IF;
END $$;

/*CREATE DATABASE ${felles_behandlingsprosess_schema_unit};

GRANT ALL PRIVILEGES ON DATABASE ${felles_behandlingsprosess_schema_unit} TO ${felles_behandlingsprosess_schema_unit};
*/

