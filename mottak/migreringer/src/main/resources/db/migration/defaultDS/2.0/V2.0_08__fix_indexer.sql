CREATE INDEX IDX_KODELISTE_6 on KODELISTE(KODEVERK);
CREATE INDEX IDX_KONFIG_VERDI_KODE_6 on KONFIG_VERDI_KODE(KONFIG_TYPE);
CREATE INDEX IDX_KONFIG_VERDI_KODE_7 on KONFIG_VERDI_KODE(KONFIG_GRUPPE);
--CREATE INDEX IDX_PROSESS_TASK_2 ON PROSESS_TASK(TASK_TYPE);
CREATE INDEX IDX_ARKIV_FILTYPE ON DOKUMENT(ARKIV_FILTYPE);
CREATE INDEX IDX_DOKUMENT_TYPE_ID ON DOKUMENT(DOKUMENT_TYPE_ID);
ALTER INDEX CHK_UNIQUE_FORS_DOKUMENT_MT RENAME TO IDX_DOKUMENT;