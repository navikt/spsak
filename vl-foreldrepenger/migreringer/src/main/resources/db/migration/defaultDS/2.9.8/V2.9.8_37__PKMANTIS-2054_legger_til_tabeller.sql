CREATE TABLE SO_OPPGITT_FRILANS (
  id                     NUMBER(19)                        NOT NULL,
  oppgitt_opptjening_id  NUMBER(19)                        NOT NULL,
  inntekt_fra_fosterhjem VARCHAR2(1)                       NOT NULL,
  nyoppstartet           VARCHAR2(1)                       NOT NULL,
  naer_relasjon          VARCHAR2(1)                       NOT NULL,
  versjon                NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av           VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid          TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av              VARCHAR2(20 CHAR),
  endret_tid             TIMESTAMP(3),
  CONSTRAINT PK_SO_OPPGITT_FRILANS PRIMARY KEY (id),
  CONSTRAINT FK_SO_OPPGITT_FRILANS FOREIGN KEY (oppgitt_opptjening_id) REFERENCES SO_OPPGITT_OPPTJENING
);

CREATE TABLE SO_OPPGITT_FRILANSOPPDRAG (
  id            NUMBER(19)                        NOT NULL,
  frilans_id    NUMBER(19)                        NOT NULL,
  fom           DATE                              NOT NULL,
  tom           DATE                              NOT NULL,
  oppdragsgiver VARCHAR2(100 CHAR)                NOT NULL,
  versjon       NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av  VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av     VARCHAR2(20 CHAR),
  endret_tid    TIMESTAMP(3),
  CONSTRAINT PK_SO_OPPGITT_FRILANSOPPDRAG PRIMARY KEY (id),
  CONSTRAINT FK_SO_OPPGITT_FRILANSOPPDRAG FOREIGN KEY (frilans_id) REFERENCES SO_OPPGITT_FRILANS
);

CREATE SEQUENCE SEQ_SO_OPPGITT_FRILANS MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_SO_OPPGITT_FRILANSOPPDRAG MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE INDEX IDX_SO_OPPGITT_F_1
  ON SO_OPPGITT_FRILANS (oppgitt_opptjening_id);
CREATE INDEX IDX_SO_OPPGITT_FO_1
  ON SO_OPPGITT_FRILANSOPPDRAG (frilans_id);

COMMENT ON TABLE SO_OPPGITT_FRILANS IS 'Frilans oppgitt av søker';
COMMENT ON COLUMN SO_OPPGITT_FRILANS.ID IS 'Primary Key';
COMMENT ON COLUMN SO_OPPGITT_FRILANS.oppgitt_opptjening_id IS 'FOREIGN KEY';
COMMENT ON COLUMN SO_OPPGITT_FRILANS.nyoppstartet IS 'J hvis nyoppstartet';
COMMENT ON COLUMN SO_OPPGITT_FRILANS.naer_relasjon IS 'J hvis nær relasjon ';
COMMENT ON COLUMN SO_OPPGITT_FRILANS.inntekt_fra_fosterhjem IS 'J hvis inntekt fra forsterhjem';

COMMENT ON TABLE SO_OPPGITT_FRILANSOPPDRAG IS 'Frilansoppdrag oppgitt av søker';
COMMENT ON COLUMN SO_OPPGITT_FRILANSOPPDRAG.ID IS 'Primary Key';
COMMENT ON COLUMN SO_OPPGITT_FRILANSOPPDRAG.frilans_id IS 'FOREIGN KEY';
COMMENT ON COLUMN SO_OPPGITT_FRILANSOPPDRAG.fom IS 'Periode start';
COMMENT ON COLUMN SO_OPPGITT_FRILANSOPPDRAG.tom IS 'Periode slutt';
COMMENT ON COLUMN SO_OPPGITT_FRILANSOPPDRAG.oppdragsgiver IS 'Oppdragsgiver';
