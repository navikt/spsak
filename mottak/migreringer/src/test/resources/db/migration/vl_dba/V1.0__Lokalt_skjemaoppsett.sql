-- ##################################################
-- ### Opplegg for enhetstester (lokal + jenkins) ###
-- ##################################################
/*DECLARE userexists INTEGER;
BEGIN
  SELECT count(*)
  INTO userexists
  FROM SYS.ALL_USERS
  WHERE USERNAME = upper('DOLLAR{vl_fordeling_schema_unit}');
  IF (userexists = 0)
  THEN
    EXECUTE IMMEDIATE ('CREATE USER DOLLAR{vl_fordeling_schema_unit} IDENTIFIED BY DOLLAR{vl_fordeling_schema_unit}');
  END IF;
END;
/

GRANT CONNECT, RESOURCE, CREATE JOB, CREATE TABLE, CREATE SYNONYM, CREATE VIEW, CREATE MATERIALIZED VIEW TO DOLLAR{vl_fordeling_schema_unit};

-- ###############################
-- ### Opplegg for lokal jetty ###
-- ###############################
DECLARE userexists INTEGER;
BEGIN
  SELECT count(*)
  INTO userexists
  FROM SYS.ALL_USERS
  WHERE USERNAME = 'FPFORDEL';
  IF (userexists = 0)
  THEN
    EXECUTE IMMEDIATE ('CREATE USER FPFORDEL IDENTIFIED BY fpfordel');
  END IF;
END;
/

GRANT CONNECT, RESOURCE, CREATE JOB, CREATE TABLE, CREATE SYNONYM, CREATE VIEW, CREATE MATERIALIZED VIEW TO FPFORDEL;
*/