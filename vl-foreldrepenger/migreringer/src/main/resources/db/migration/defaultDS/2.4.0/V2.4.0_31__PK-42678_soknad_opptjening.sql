-- KODEVERK
INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('VIRKSOMHET_TYPE', 'N', 'N', 'Virksomhet type', 'Kodeverk for virksomhetstyper');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'VIRKSOMHET_TYPE', 'VANLIG', 'VANLIG', 'Vanlig', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'), 'VIRKSOMHET_TYPE');

INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('ANNEN_OPPTJENING_TYPE', 'N', 'N', 'Annen opptjening type', 'Kodeverk for annen opptjening');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ANNEN_OPPTJENING_TYPE', 'VANLIG', 'VANLIG', 'Vanlig', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'), 'ANNEN_OPPTJENING_TYPE');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ANNEN_OPPTJENING_TYPE', 'VENTELØNN', null, 'Ventelønn', NULL, to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ANNEN_OPPTJENING_TYPE', 'LØNN_UNDER_UTDANNING', null, 'Lønn under utdanning', NULL, to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ANNEN_OPPTJENING_TYPE', 'MILITÆR_ELLER_SIVILTJENESTE', null, 'Militær eller siviltjeneste', NULL, to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ANNEN_OPPTJENING_TYPE', 'SLUTTPAKKE', null, 'Sluttpakke', NULL, to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ANNEN_OPPTJENING_TYPE', 'VARTPENGER', null, 'Vartpenger', NULL, to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNTEKTS_KILDE', 'SIGRUN', 'SIGRUN', 'Sigrun', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
-- KODEVERK SLUTT

CREATE TABLE SO_OPPGITT_OPPTJENING (
  ID            NUMBER(19)                        NOT NULL,
  opprettet_av  VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av     VARCHAR2(20 CHAR),
  endret_tid    TIMESTAMP(3),
  CONSTRAINT PK_OPPGITT_OPPTJENING PRIMARY KEY (ID)
);

CREATE SEQUENCE SEQ_SO_OPPGITT_OPPTJENING MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

ALTER TABLE GR_ARBEID_INNTEKT
  ADD OPPGITT_OPPTJENING_ID NUMBER(19);

CREATE TABLE OPPGITT_ARBEIDSFORHOLD (
  ID                         NUMBER(19)                        NOT NULL,
  OPPGITT_OPPTJENING_ID      NUMBER(19)                        NOT NULL,
  VIRKSOMHET_ID              NUMBER(19),
  fom                        DATE                              NOT NULL,
  tom                        DATE                              NOT NULL,
  UTENLANDSK_INNTEKT         VARCHAR2(1)                       NOT NULL,
  arbeid_type                VARCHAR2(100)                     NOT NULL,
  KL_arbeid_type             VARCHAR2(100) AS ('ARBEID_TYPE'),
  utenlandsk_virksomhet_navn VARCHAR2(100),
  LAND                       VARCHAR2(100),
  KL_LANDKODER               VARCHAR2(100) DEFAULT 'LANDKODER' NOT NULL,
  opprettet_av               VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid              TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                  VARCHAR2(20 CHAR),
  endret_tid                 TIMESTAMP(3),
  CONSTRAINT PK_ARBEIDSFORHOLD PRIMARY KEY (ID),
  CONSTRAINT FK_ARBEIDSFORHOLD_1 FOREIGN KEY (OPPGITT_OPPTJENING_ID) REFERENCES SO_OPPGITT_OPPTJENING (ID),
  CONSTRAINT FK_ARBEIDSFORHOLD_2 FOREIGN KEY (VIRKSOMHET_ID) REFERENCES VIRKSOMHET (ID)
);

CREATE SEQUENCE SEQ_OPPGITT_ARBEIDSFORHOLD MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE TABLE STILLING (
  ID                     NUMBER(19)                        NOT NULL,
  OPPGITT_ARBEIDSFORHOLD NUMBER(19)                        NOT NULL,
  fom                    DATE                              NOT NULL,
  tom                    DATE                              NOT NULL,
  fast_stillingsprosent  NUMBER(5, 2)                      NOT NULL,
  variable_stilling      VARCHAR2(1)                       NOT NULL,
  fast_og_variable       VARCHAR2(1)                       NOT NULL,
  opprettet_av           VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid          TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av              VARCHAR2(20 CHAR),
  endret_tid             TIMESTAMP(3),
  CONSTRAINT PK_STILLING PRIMARY KEY (ID),
  CONSTRAINT FK_STILLING_1 FOREIGN KEY (OPPGITT_ARBEIDSFORHOLD) REFERENCES OPPGITT_ARBEIDSFORHOLD (ID)
);

CREATE SEQUENCE SEQ_STILLING MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE TABLE EGEN_NAERING (
  ID                         NUMBER(19)                              NOT NULL,
  OPPGITT_OPPTJENING_ID      NUMBER(19)                              NOT NULL,
  fom                        DATE                                    NOT NULL,
  tom                        DATE                                    NOT NULL,
  KL_VIRKSOMHET_TYPE        VARCHAR2(100) DEFAULT 'VIRKSOMHET_TYPE' NOT NULL,
  VIRKSOMHET_TYPE            VARCHAR2(100),
  VIRKSOMHET_ID              NUMBER(19),
  regnskapsfoerer_navn       VARCHAR2(100)                           NOT NULL,
  regnskapsfoerer_tlf        VARCHAR2(100)                           NOT NULL,
  endring_dato               DATE,
  begrunnelse                VARCHAR2(100),
  brutto_inntekt             NUMBER(10, 2),
  utenlandsk_virksomhet_navn VARCHAR2(100),
  LAND                       VARCHAR2(100),
  KL_LANDKODER               VARCHAR2(100) DEFAULT 'LANDKODER'       NOT NULL,
  opprettet_av               VARCHAR2(20 CHAR) DEFAULT 'VL'          NOT NULL,
  opprettet_tid              TIMESTAMP(3) DEFAULT systimestamp       NOT NULL,
  endret_av                  VARCHAR2(20 CHAR),
  endret_tid                 TIMESTAMP(3),
  CONSTRAINT PK_EGEN_NAERING PRIMARY KEY (ID),
  CONSTRAINT FK_EGEN_NAERING_1 FOREIGN KEY (OPPGITT_OPPTJENING_ID) REFERENCES SO_OPPGITT_OPPTJENING (ID),
  CONSTRAINT FK_EGEN_NAERING_2 FOREIGN KEY (VIRKSOMHET_ID) REFERENCES VIRKSOMHET (ID),
  CONSTRAINT FK_EGEN_NAERING_3 FOREIGN KEY (KL_VIRKSOMHET_TYPE, VIRKSOMHET_TYPE) REFERENCES KODELISTE (kodeverk, kode)
);

CREATE SEQUENCE SEQ_EGEN_NAERING MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE TABLE ANNEN_AKTIVITET (
  ID                       NUMBER(19)                                                   NOT NULL,
  OPPGITT_OPPTJENING_ID    NUMBER(19)                                                   NOT NULL,
  fom                      DATE                                                         NOT NULL,
  tom                      DATE                                                         NOT NULL,
  KL_ANNEN_OPPTJENING_TYPE VARCHAR2(100) DEFAULT 'ANNEN_OPPTJENING_TYPE'                NOT NULL,
  ANNEN_OPPTJENING_TYPE    VARCHAR2(100)                                                NOT NULL,
  opprettet_av             VARCHAR2(20 CHAR) DEFAULT 'VL'                               NOT NULL,
  opprettet_tid            TIMESTAMP(3) DEFAULT systimestamp                            NOT NULL,
  endret_av                VARCHAR2(20 CHAR),
  endret_tid               TIMESTAMP(3),
  CONSTRAINT PK_ANNEN_AKTIVITET PRIMARY KEY (ID),
  CONSTRAINT FK_ANNEN_AKTIVITET_1 FOREIGN KEY (OPPGITT_OPPTJENING_ID) REFERENCES SO_OPPGITT_OPPTJENING (ID),
  CONSTRAINT FK_ANNEN_AKTIVITET_2 FOREIGN KEY (KL_ANNEN_OPPTJENING_TYPE, ANNEN_OPPTJENING_TYPE) REFERENCES KODELISTE (kodeverk, kode)
);

CREATE SEQUENCE SEQ_ANNEN_AKTIVITET MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

-- myker opp GR_ARBEID_INNTEKT

-- legger til på søknad
ALTER TABLE SOEKNAD ADD OPPGITT_OPPTJENING_ID NUMBER(19);
