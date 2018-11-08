UPDATE SATS set TOM = TO_DATE('2017-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS') where ID = 1;

Declare

  antall number;

BEGIN
  select count(*) into antall from SATS where SATS_TYPE = 'ENGANG' and FOM = TO_DATE('2018-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS');
  IF (antall = 0) THEN
    INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI, VERSJON, OPPRETTET_AV, OPPRETTET_TID)
    VALUES (seq_sats.nextval, 'ENGANG', TO_DATE('2018-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 63140, 0, 'VL', SYSTIMESTAMP);
  END IF;
END;
