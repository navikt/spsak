-- TODO ?? Partisjonering
/*Declare

  antall number;
  opprett_tmp_tabell VARCHAR(75) := 'Create table PROSESS_TASK_TMP as select * from PROSESS_TASK';

  dropp_process_tabell VARCHAR(75) := 'Drop table PROSESS_TASK cascade constraints';

  copydata VARCHAR(75) := 'Insert into PROSESS_TASK select * from PROSESS_TASK_TMP';

  dropp_tmp_tabell VARCHAR(75) := 'Drop table PROSESS_TASK_TMP cascade constraints';

  opprett_process_tabell VARCHAR(2000) := 'CREATE TABLE PROSESS_TASK ' ||
                                       ' ( ID NUMBER(19,0) NOT NULL, ' ||
                                       ' TASK_TYPE VARCHAR(200) NOT NULL, ' ||
                                       ' PRIORITET integer DEFAULT 0 NOT NULL, ' ||
                                       ' STATUS VARCHAR(20) DEFAULT ''KLAR'' NOT NULL, ' ||
                                       ' TASK_PARAMETERE VARCHAR(4000), ' ||
                                       ' TASK_PAYLOAD TEXT, ' ||
                                       ' TASK_GRUPPE VARCHAR(250), ' ||
                                       ' TASK_SEKVENS VARCHAR(100) DEFAULT ''1'' NOT NULL, ' ||
                                       ' NESTE_KJOERING_ETTER TIMESTAMP (0) DEFAULT current_timestamp, ' ||
                                       ' FEILEDE_FORSOEK integer DEFAULT 0, ' ||
                                       ' SISTE_KJOERING_TS TIMESTAMP (6), ' ||
                                       ' SISTE_KJOERING_FEIL_KODE VARCHAR(50), ' ||
                                       ' SISTE_KJOERING_FEIL_TEKST TEXT, ' ||
                                       ' SISTE_KJOERING_SERVER VARCHAR(50), ' ||
                                       ' VERSJON NUMBER(19,0) DEFAULT 0 NOT NULL, ' ||
                                       ' CONSTRAINT CHK_PROSESS_TASK_STATUS CHECK (status IN (''KLAR'', ''FEILET'', ''VENTER_SVAR'', ''SUSPENDERT'', ''FERDIG'')), ' ||
                                       ' CONSTRAINT PK_PROSESS_TASK PRIMARY KEY (ID), ' ||
                                       ' CONSTRAINT FK_PROSESS_TASK_1 FOREIGN KEY (TASK_TYPE)REFERENCES PROSESS_TASK_TYPE (KODE)) row movement';

  legg_partisjon VARCHAR(255) := ' PARTITION by list (status)(' ||
                                      ' PARTITION status_ferdig values (''FERDIG''),' ||
                                      ' PARTITION status_feilet values (''FEILET''),' ||
                                      ' PARTITION status_klar values(''KLAR'', ''VENTER_SVAR'', ''SUSPENDERT''))';

BEGIN

  select count(*) into antall from USER_TABLES where TABLE_NAME = 'PROSESS_TASK';
  IF (antall = 1) THEN
    execute immediate opprett_tmp_tabell;
    execute immediate dropp_process_tabell;
  END IF;

  IF (DBMS_DB_VERSION.VERSION < 12) THEN
    execute immediate opprett_process_tabell;
  ELSE
    execute immediate opprett_process_tabell || legg_partisjon;
  END IF;

  IF (antall = 1) THEN
    execute immediate copydata;
    execute immediate dropp_tmp_tabell;
  END IF;
END;
*/