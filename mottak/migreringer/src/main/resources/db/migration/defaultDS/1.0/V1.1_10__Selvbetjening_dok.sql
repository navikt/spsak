CREATE TABLE DOKUMENT
(
  ID               NUMBER(19, 0),
  FORSENDELSE_ID   RAW(16),
  DOKUMENT_TYPE_ID VARCHAR2(100),
  CLOB         CLOB,
  HOVED_DOKUMENT   CHAR(1),
  CONTENT_TYPE     VARCHAR2(100),
  OPPRETTET_AV     VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  OPPRETTET_TID    TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  ENDRET_AV        VARCHAR2(20 CHAR),
  ENDRET_TID       TIMESTAMP(3)
);
CREATE SEQUENCE SEQ_DOKUMENT MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON COLUMN DOKUMENT.ID IS 'Primærnøkkel';
COMMENT ON COLUMN DOKUMENT.FORSENDELSE_ID IS 'Unik ID for forsendelsen';
COMMENT ON COLUMN DOKUMENT.DOKUMENT_TYPE_ID IS 'Kodeverdi for innkommende dokument type';
COMMENT ON COLUMN DOKUMENT.CLOB IS 'Innkommende dokument';
COMMENT ON COLUMN DOKUMENT.HOVED_DOKUMENT IS 'Er dette hoveddokument? (J/N)';
COMMENT ON COLUMN DOKUMENT.CONTENT_TYPE IS 'type innhold';

ALTER TABLE DOKUMENT
  ADD CONSTRAINT PK_DOKUMENT PRIMARY KEY (ID);
ALTER TABLE DOKUMENT
  ADD KL_DOKUMENT_TYPE_ID VARCHAR(100) GENERATED ALWAYS AS ('DOKUMENT_TYPE_ID') VIRTUAL;
ALTER TABLE DOKUMENT
  ADD CONSTRAINT FK_MOTTATT_DOKUMENT_01 FOREIGN KEY (DOKUMENT_TYPE_ID, KL_DOKUMENT_TYPE_ID) REFERENCES KODELISTE (kode, kodeverk);

CREATE TABLE DOKUMENT_METADATA
(
  ID             NUMBER(19, 0),
  FORSENDELSE_ID RAW(16),
  AVSENDER_ID    VARCHAR2(11),
  SAKSNUMMER     VARCHAR2(32),
  ARKIV_ID       VARCHAR2(32),
  OPPRETTET_AV   VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  OPPRETTET_TID  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  ENDRET_AV      VARCHAR2(20 CHAR),
  ENDRET_TID     TIMESTAMP(3)
);
CREATE SEQUENCE SEQ_DOKUMENT_METADATA MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON COLUMN DOKUMENT_METADATA.ID IS 'Primærnøkkel';
COMMENT ON COLUMN DOKUMENT_METADATA.FORSENDELSE_ID IS 'Unik ID for forsendelsen';
COMMENT ON COLUMN DOKUMENT_METADATA.AVSENDER_ID IS 'ID til avsenderen av et dokument';
COMMENT ON COLUMN DOKUMENT_METADATA.SAKSNUMMER IS 'ID til fagsak et dokument knyttes mot';
COMMENT ON COLUMN DOKUMENT_METADATA.ARKIV_ID IS 'ID til dokumentet i JOARK';

ALTER TABLE DOKUMENT_METADATA
  ADD CONSTRAINT PK_DOKUMENT_METADATA PRIMARY KEY (ID);
ALTER TABLE DOKUMENT_METADATA
  ADD CONSTRAINT CHK_UNIQUE_FORS_DOKUMENT_MT UNIQUE (FORSENDELSE_ID);