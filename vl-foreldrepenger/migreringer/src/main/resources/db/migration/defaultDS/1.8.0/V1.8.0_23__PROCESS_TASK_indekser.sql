CREATE INDEX IDX_PROSESS_TASK_2 ON PROSESS_TASK(task_type);
CREATE INDEX IDX_PROSESS_TASK_3 ON PROSESS_TASK(neste_kjoering_etter);

-- CREATE INDEX IDX_PROSESS_TASK_4 ON PROSESS_TASK(task_parametere);
CREATE INDEX IDX_PROSESS_TASK_5 ON PROSESS_TASK(task_gruppe);


Declare
  legg_til_indeks varchar2(66) := 'CREATE INDEX IDX_PROSESS_TASK_1 ON PROSESS_TASK(status) ';

BEGIN

  IF (DBMS_DB_VERSION.VERSION < 12) THEN
    execute immediate legg_til_indeks;
  ELSE
    execute immediate legg_til_indeks || ' LOCAL';
  END IF;

END;
