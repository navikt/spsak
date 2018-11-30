-- ###############################
-- ### Opplegg for lokal jetty ###
-- ###############################
DECLARE userexists INTEGER;
BEGIN
  SELECT count(*)
  INTO userexists
  FROM SYS.ALL_USERS
  WHERE USERNAME = 'FPSAK';
  IF (userexists = 0)
  THEN
    EXECUTE IMMEDIATE ('CREATE USER FPSAK IDENTIFIED BY fpsak');
  END IF;
END;
/

DECLARE userexists INTEGER;
BEGIN
  SELECT count(*)
  INTO userexists
  FROM SYS.ALL_USERS
  WHERE USERNAME = 'FPSAK_HIST';
  IF (userexists = 0)
  THEN
    EXECUTE IMMEDIATE ('CREATE USER FPSAK_HIST IDENTIFIED BY fpsak_hist');
  END IF;
END;
/

GRANT CONNECT, RESOURCE, CREATE JOB, CREATE TABLE, CREATE SYNONYM, CREATE VIEW, CREATE MATERIALIZED VIEW TO FPSAK;
GRANT CONNECT, RESOURCE, CREATE JOB, CREATE TABLE, CREATE SYNONYM, CREATE VIEW, CREATE MATERIALIZED VIEW TO FPSAK_HIST;

-- Ikke endre rollenavn, den er referert i migreringsskriptene og skal finnes i alle miljøer inkl prod
DECLARE roleexists INTEGER;
BEGIN
  SELECT count(*)
  INTO roleexists
  FROM SYS.DBA_ROLES
  WHERE ROLE = 'FPSAK_HIST_SKRIVE_ROLE';
  IF (roleexists = 0)
  THEN
    EXECUTE IMMEDIATE ('CREATE ROLE FPSAK_HIST_SKRIVE_ROLE');
  END IF;
END;
/

-- Ikke endre rollenavn, den er referert i migreringsskriptene og skal finnes i alle miljøer inkl prod
DECLARE roleexists INTEGER;
BEGIN
  SELECT count(*)
  INTO roleexists
  FROM SYS.DBA_ROLES
  WHERE ROLE = 'FPSAK_HIST_LESE_ROLE';
  IF (roleexists = 0)
  THEN
    EXECUTE IMMEDIATE ('CREATE ROLE FPSAK_HIST_LESE_ROLE');
  END IF;
END;
/


GRANT FPSAK_HIST_SKRIVE_ROLE TO FPSAK;

-- ##################################################
-- ### Opplegg for enhetstester (lokal + jenkins) ###
-- ##################################################
DECLARE userexists INTEGER;
BEGIN
  SELECT count(*)
  INTO userexists
  FROM SYS.ALL_USERS
  WHERE USERNAME = upper('${vl_fpsak_schema_unit}');
  IF (userexists = 0)
  THEN
    EXECUTE IMMEDIATE ('CREATE USER ${vl_fpsak_schema_unit} IDENTIFIED BY ${vl_fpsak_schema_unit}');
  END IF;
END;
/

DECLARE userexists INTEGER;
BEGIN
  SELECT count(*)
  INTO userexists
  FROM SYS.ALL_USERS
  WHERE USERNAME = upper('${vl_fpsak_hist_schema_unit}');
  IF (userexists = 0)
  THEN
    EXECUTE IMMEDIATE ('CREATE USER ${vl_fpsak_hist_schema_unit} IDENTIFIED BY ${vl_fpsak_hist_schema_unit}');
  END IF;
END;
/

GRANT CONNECT, RESOURCE, CREATE JOB, CREATE TABLE, CREATE SYNONYM, CREATE VIEW, CREATE MATERIALIZED VIEW TO ${vl_fpsak_schema_unit};
GRANT CONNECT, RESOURCE, CREATE JOB, CREATE TABLE, CREATE SYNONYM, CREATE VIEW, CREATE MATERIALIZED VIEW TO ${vl_fpsak_hist_schema_unit};

-- Ikke endre rollenavn, den er referert i migreringsskriptene og skal finnes i alle miljøer inkl prod
GRANT FPSAK_HIST_SKRIVE_ROLE TO ${vl_fpsak_schema_unit};
