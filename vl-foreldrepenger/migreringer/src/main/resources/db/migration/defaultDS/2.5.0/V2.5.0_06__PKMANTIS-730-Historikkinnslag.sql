create table HISTORIKKINNSLAG_DEL (
  id              NUMBER(19) NOT NULL,
  HISTORIKKINNSLAG_ID       NUMBER(19) NOT NULL,
  versjon         NUMBER(19) DEFAULT 0 NOT NULL,
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_HISTORIKKINNSLAG_DEL PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_HISTORIKKINNSLAG_DEL MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE INDEX IDX_HISTORIKKINNSLAG_DEL_1 ON HISTORIKKINNSLAG_DEL (HISTORIKKINNSLAG_ID);

ALTER TABLE HISTORIKKINNSLAG_DEL ADD CONSTRAINT FK_HISTORIKKINNSLAG_DEL_1 FOREIGN KEY ( HISTORIKKINNSLAG_ID ) REFERENCES HISTORIKKINNSLAG ( id ) ;

create table HISTORIKKINNSLAG_FELT (
  id                      NUMBER(19) NOT NULL,
  HISTORIKKINNSLAG_DEL_ID NUMBER(19) NOT NULL,
  NAVN                    VARCHAR(100) NOT NULL,
  FRA_VERDI                   VARCHAR(4000) NULL,
  TIL_VERDI                   VARCHAR(4000) NOT NULL,
  KL_VERDI                VARCHAR(100) NULL,
  SEKVENS_NR              NUMBER(5,0) NULL,
  versjon         NUMBER(19) DEFAULT 0 NOT NULL,
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_HISTORIKKINNSLAG_FELT PRIMARY KEY (id)
);

CREATE INDEX IDX_HISTORIKKINNSLAG_FELT_1 ON HISTORIKKINNSLAG_FELT (HISTORIKKINNSLAG_DEL_ID);

ALTER TABLE HISTORIKKINNSLAG_FELT ADD CONSTRAINT FK_HISTORIKKINNSLAG_FELT_1 FOREIGN KEY ( HISTORIKKINNSLAG_DEL_ID ) REFERENCES HISTORIKKINNSLAG_DEL ( id ) ;

CREATE SEQUENCE SEQ_HISTORIKKINNSLAG_FELT MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE HISTORIKKINNSLAG_DEL   IS 'Et historikkinnslag kan ha en eller flere deler';
COMMENT ON TABLE HISTORIKKINNSLAG_FELT  IS 'En historikkinnslagdel har typisk mange felt';

COMMENT ON COLUMN HISTORIKKINNSLAG_DEL.HISTORIKKINNSLAG_ID IS 'FK: HISTORIKKINNSLAG';
COMMENT ON COLUMN HISTORIKKINNSLAG_FELT.NAVN IS 'Navn på felt';
COMMENT ON COLUMN HISTORIKKINNSLAG_FELT.FRA_VERDI IS 'Feltets gamle verdi. Kan være kodeverk eller en string';
COMMENT ON COLUMN HISTORIKKINNSLAG_FELT.TIL_VERDI IS 'Feltets nye verdi. Kan være kodeverk eller en string';
COMMENT ON COLUMN HISTORIKKINNSLAG_FELT.KL_VERDI IS 'FK: KODELISTE';
COMMENT ON COLUMN HISTORIKKINNSLAG_FELT.SEKVENS_NR IS 'Settes dersom historikkinnslagdelen har flere innslag med samme navn';

INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES ('HISTORIKKINNSLAG_FELT_TYPE', 'Kodeverk for endrede felt i historikkinnslag', '','VL');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'AARSAK', 'aarsak', 'Årsak', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'BEGRUNNELSE', 'begrunnelse', 'Begrunnelse', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'HENDELSE', 'hendelse', 'Hendelse', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'OPPLYSNINGER', 'opplysninger', 'Opplysninger', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'ENDREDE_FELTER', 'endredeFelter', 'Endrede felter', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'SKJERMLINKE', 'skjermlinke', 'Skjermlinke', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');

-- aarsak
-- begrunnelse
-- hendelse
-- opplysninger (verdi, navn)
-- endredeFelter (navn, fraVerdi, tilVerdi)
-- skjermlinke ('faktaNavn+punktNavn')
