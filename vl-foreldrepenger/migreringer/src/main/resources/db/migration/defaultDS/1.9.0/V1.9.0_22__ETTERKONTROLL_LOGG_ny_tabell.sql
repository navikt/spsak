CREATE TABLE ETTERKONTROLL_LOGG (
  id            NUMBER(19, 0)                     NOT NULL,
  behandling_id NUMBER(19, 0)                     NOT NULL,
  versjon       NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av  VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av     VARCHAR2(20 CHAR),
  endret_tid    TIMESTAMP(3),
  CONSTRAINT PK_ETTERKONTROLL_LOGG PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_ETTERKONTROLL_LOGG MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

ALTER TABLE ETTERKONTROLL_LOGG ADD CONSTRAINT FK_ETTERKONTROLL_LOGG_1 FOREIGN KEY (behandling_id) REFERENCES BEHANDLING;
CREATE INDEX IDX_ETTERKONTROLL_LOGG_1 ON ETTERKONTROLL_LOGG (behandling_id)
