CREATE TABLE MEDLEMSKAP_VURDERING_PERIODE (
  id                NUMBER(19)                        NOT NULL,
  versjon           NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av      VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid     TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av         VARCHAR2(20 CHAR),
  endret_tid        TIMESTAMP(3),
  CONSTRAINT PK_MEDLEMSKAP_VP PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_MEDLEMSKAP_VP MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE MEDLEMSKAP_VURDERING_PERIODE  IS 'En koblingstabell som holder vurderte periode for løpende medlemskap';
COMMENT ON COLUMN MEDLEMSKAP_VURDERING_PERIODE.ID IS 'Primær nøkkel for medlemskapvurdering_periode';
