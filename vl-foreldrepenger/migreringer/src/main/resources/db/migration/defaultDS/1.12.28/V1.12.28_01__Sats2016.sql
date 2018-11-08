merge into sats s using dual on (dual.dummy is not null and s.verdi = 46000)
when not matched then
  insert (ID, SATS_TYPE, FOM, TOM, VERDI, VERSJON, OPPRETTET_AV, OPPRETTET_TID)
  VALUES (SEQ_SATS.nextval, 'ENGANG', TO_DATE('2016-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS') ,TO_DATE('2016-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 46000, 0, 'VL', SYSTIMESTAMP);
