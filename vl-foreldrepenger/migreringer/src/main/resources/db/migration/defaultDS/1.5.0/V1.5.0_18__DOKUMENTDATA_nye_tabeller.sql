-- Tabell SPRAAK_KODE
CREATE TABLE SPRAAK_KODE (
  kode            VARCHAR2(20 CHAR) NOT NULL,
  navn            VARCHAR2(40 CHAR) NOT NULL,
  beskrivelse     VARCHAR2(4000 CHAR),
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_SPRAAK_KODE PRIMARY KEY (kode)
);
INSERT INTO SPRAAK_KODE (kode, navn) VALUES ('nb', 'Norsk bokmål');
INSERT INTO SPRAAK_KODE (kode, navn) VALUES ('nn', 'Norsk nynorsk');
INSERT INTO SPRAAK_KODE (kode, navn) VALUES ('en', 'Engelsk');

-- Tabell ADRESSE
CREATE TABLE ADRESSE (
  id                 NUMBER(19) NOT NULL,
  versjon            NUMBER(19) DEFAULT 0 NOT NULL,
  mottakerNavn       VARCHAR2(40 CHAR),
  adresselinje1      VARCHAR2(40 CHAR),
  adresselinje2      VARCHAR2(40 CHAR),
  adresselinje3      VARCHAR2(40 CHAR),
  post_nummer        VARCHAR2(20 CHAR),
  poststed           VARCHAR2(40 CHAR),
  land               VARCHAR2(40 CHAR),
  opprettet_av       VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid      TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av          VARCHAR2(20 CHAR),
  endret_tid         TIMESTAMP(3),
  CONSTRAINT PK_ADRESSE PRIMARY KEY ( id )
);

CREATE SEQUENCE SEQ_ADRESSE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;


-- Tabell DOKUMENT_DATA
CREATE TABLE DOKUMENT_DATA (
  id                 NUMBER(19) NOT NULL,
  behandling_id      NUMBER(19, 0) NOT NULL,
  journal_post_id    VARCHAR2(20 CHAR),
  dokument_id        VARCHAR2(20 CHAR),
  dokument_mal_navn  VARCHAR2(80 CHAR) NOT NULL,
  forhaandsvist_tid  TIMESTAMP(3),
  sendt_tid          TIMESTAMP(3),
  dokument_felles_id NUMBER(19),
  versjon            NUMBER(19) DEFAULT 0 NOT NULL,
  opprettet_av       VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid      TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av          VARCHAR2(20 CHAR),
  endret_tid         TIMESTAMP(3),
  CONSTRAINT PK_DOKUMENT_DATA PRIMARY KEY ( id )
);

ALTER TABLE DOKUMENT_DATA ADD CONSTRAINT FK_DOKUMENT_DATA_2 FOREIGN KEY ( behandling_id ) REFERENCES BEHANDLING ( id );
CREATE INDEX IDX_DOKUMENT_DATA_2 ON DOKUMENT_DATA(behandling_id);

CREATE SEQUENCE SEQ_DOKUMENT_DATA MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

-- Tabell DOKUMENT_FELLES
CREATE TABLE DOKUMENT_FELLES (
  id                        NUMBER(19) NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0 NOT NULL,
  spraak_kode               VARCHAR2(7 CHAR) NOT NULL,
  saksnummer                NUMBER(19) NOT NULL,
  sign_saksbehandler_navn   VARCHAR2(80 CHAR),
  automatisk_behandlet      VARCHAR2(1 CHAR) NOT NULL,
  sakspart_id               VARCHAR2(20 CHAR) NOT NULL,
  sakspart_navn             VARCHAR2(80 CHAR) NOT NULL,
  sign_beslutter_navn       VARCHAR2(80 CHAR),
  sign_beslutter_geo_enhet  VARCHAR2(80 CHAR),
  mottaker_id               VARCHAR2(20 CHAR) NOT NULL,
  mottaker_navn             VARCHAR2(80 CHAR) NOT NULL,
  mottaker_adresse_id       NUMBER(19) NOT NULL,
  navn_avsender_enhet       VARCHAR2(80 CHAR),
  nummer_avsender_enhet     VARCHAR2(80 CHAR),
  kontakt_telefon_nummer    VARCHAR2(80 CHAR),
  retur_adresse_id          NUMBER(19) NOT NULL,
  post_adresse_id           NUMBER(19) NOT NULL,
  dokument_dato             TIMESTAMP(3),

  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_DOKUMENT_FELLES PRIMARY KEY ( id )
);
ALTER TABLE DOKUMENT_FELLES ADD CONSTRAINT FK_DOKUMENT_FELLES_1 FOREIGN KEY ( mottaker_adresse_id ) REFERENCES ADRESSE ( id );
ALTER TABLE DOKUMENT_FELLES ADD CONSTRAINT FK_DOKUMENT_FELLES_2 FOREIGN KEY ( retur_adresse_id ) REFERENCES ADRESSE ( id );
ALTER TABLE DOKUMENT_FELLES ADD CONSTRAINT FK_DOKUMENT_FELLES_3 FOREIGN KEY ( post_adresse_id ) REFERENCES ADRESSE ( id );
ALTER TABLE DOKUMENT_FELLES ADD CONSTRAINT FK_DOKUMENT_FELLES_4 FOREIGN KEY ( spraak_kode ) REFERENCES SPRAAK_KODE ( kode );

CREATE SEQUENCE SEQ_DOKUMENT_FELLES MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

ALTER TABLE BEHANDLING ADD SPRAAK_KODE VARCHAR2(7 CHAR) DEFAULT 'nb' NOT NULL;
ALTER TABLE BEHANDLING ADD CONSTRAINT FK_BEHANDLING_5 FOREIGN KEY ( SPRAAK_KODE ) REFERENCES SPRAAK_KODE ( kode );

-- Tabell DOKUMENT_TYPE_DATA
CREATE TABLE DOKUMENT_TYPE_DATA (
  id                        NUMBER(19) NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0 NOT NULL,
  doksys_id                 VARCHAR2(40 CHAR) NOT NULL,
  verdi                     VARCHAR2(4000 CHAR) NOT NULL,
  dokument_felles_id        NUMBER(19) NOT NULL,
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_DOKUMENT_TYPE_DATA PRIMARY KEY ( id )
);
ALTER TABLE DOKUMENT_TYPE_DATA ADD CONSTRAINT FK_DOKUMENT_TYPE_DATA_1 FOREIGN KEY ( dokument_felles_id ) REFERENCES DOKUMENT_FELLES ( id );

ALTER TABLE DOKUMENT_DATA ADD CONSTRAINT FK_DOKUMENT_DATA_1 FOREIGN KEY ( dokument_felles_id ) REFERENCES DOKUMENT_FELLES ( id );

CREATE SEQUENCE SEQ_DOKUMENT_TYPE_DATA MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
