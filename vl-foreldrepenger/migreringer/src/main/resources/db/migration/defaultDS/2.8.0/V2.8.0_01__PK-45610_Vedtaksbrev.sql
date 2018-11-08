-- PK-45610 Støtte for strukturerte flettefelter
alter table DOKUMENT_TYPE_DATA add STRUKTURERT_VERDI clob NULL;
alter table DOKUMENT_TYPE_DATA modify (VERDI NULL);

-- DOKUMENT_MAL_TYPE ny rad for innvilgelsesbrev FP
INSERT INTO DOKUMENT_MAL_TYPE (kode, navn, generisk, DOKSYS_KODE) VALUES ('INNVFP', 'Innvilgelsesbrev Foreldrepenger', 'N', '000061');
