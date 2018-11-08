--ny tabell GR_ARBEID_INNTEKT START
CREATE TABLE GR_ARBEID_INNTEKT (
  id                        NUMBER(19)                        NOT NULL,
  behandling_id             NUMBER(19)                        NOT NULL,
  inntekt_arbeid_ytelser_id NUMBER(19)                        NOT NULL,
  overstyrt_id              NUMBER(19),
  aktiv                     VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_GR_ARBEID_INNTEKT PRIMARY KEY (id),
  CONSTRAINT FK_GR_ARBEID_INNTEKT_1 FOREIGN KEY (behandling_id) REFERENCES BEHANDLING,
  --CONSTRAINT FK_GR_ARBEID_INNTEKT_2 FOREIGN KEY (inntekt_arbeid_ytelser_id) REFERENCES INNTEKT_ARBEID_YTELSER,
  CONSTRAINT CHK_GR_ARBEID_INNTEKT CHECK (AKTIV IN ('J', 'N'))
);

CREATE UNIQUE INDEX UIDX_GR_ARBEID_INNTEKT_01
  ON GR_ARBEID_INNTEKT (
    (CASE WHEN AKTIV = 'J'
      THEN BEHANDLING_ID
     ELSE NULL END),
    (CASE WHEN AKTIV = 'J'
      THEN AKTIV
     ELSE NULL END)
  );

CREATE SEQUENCE SEQ_GR_ARBEID_INNTEKT MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE GR_ARBEID_INNTEKT IS 'Behandlingsgrunnlag for arbeid, inntekt og ytelser (aggregat)';
COMMENT ON COLUMN GR_ARBEID_INNTEKT.ID IS 'Primary Key';
COMMENT ON COLUMN GR_ARBEID_INNTEKT.BEHANDLING_ID IS 'FK: BEHANDLING';
COMMENT ON COLUMN GR_ARBEID_INNTEKT.inntekt_arbeid_ytelser_id IS 'FK: INNTEKT_ARBEID_YTELSER';
COMMENT ON COLUMN GR_ARBEID_INNTEKT.AKTIV IS 'Angir aktivt grunnlag for Behandling.  Kun ett innslag tillates å være aktivt(J), men mange kan være inaktive(N) ';
--GR_ARBEID_INNTEKT SLUTT


--ny tabell INNTEKT_ARBEID_YTELSER START
CREATE TABLE INNTEKT_ARBEID_YTELSER (
  id                      NUMBER(19)                        NOT NULL,
  inntekt_opplysninger_id NUMBER(19),
  arbeid_opplysninger_id  NUMBER(19),
  ytelse_opplysninger_id  NUMBER(19),
  versjon                 NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av            VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid           TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av               VARCHAR2(20 CHAR),
  endret_tid              TIMESTAMP(3),
  CONSTRAINT PK_INNTEKT_ARBEID_YTELSER PRIMARY KEY (id)
  -- CONSTRAINT FK_GR_ARBEID_INNTEKT_2 FOREIGN KEY (inntekt_arbeid_ytelser_id) REFERENCES INNTEKT_ARBEID_YTELSER
);

CREATE SEQUENCE SEQ_INNTEKT_ARBEID_YTELSER MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
-- INNTEKT_OPPLSNINGER slutt

-- ny tabell AKTOER_INNTEKT
CREATE TABLE AKTOER_INNTEKT (
  id                        NUMBER(19)                        NOT NULL,
  inntekt_arbeid_ytelser_id NUMBER(19)                        NOT NULL,
  aktoer_id                 NUMBER(19)                        NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3)
);

CREATE SEQUENCE SEQ_AKTOER_INNTEKT MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

-- AKTOER_INNTEKT slutt

-- Start -> Endre tabell INNTEKT
-- ALTER TABLE INNTEKT
--   ADD aktoer_inntekt_id NUMBER(19);
-- ALTER TABLE INNTEKT
--   ADD yrkesaktivivitet_id NUMBER(19);
-- ALTER TABLE INNTEKT
--   ADD kilde VARCHAR2(100);
-- ALTER TABLE INNTEKT
--   ADD kl_kilde VARCHAR2(100) AS ('INNTEKTS_KILDE');
-- ALTER TABLE INNTEKT
--   MODIFY BEHANDLING_GRUNNLAG_ID NULL;

CREATE TABLE TMP_INNTEKT (
  id                  NUMBER(19) NOT NULL,
  aktoer_inntekt_id   NUMBER(19),
  yrkesaktivivitet_id NUMBER(19),
  kilde               VARCHAR2(100),
  kl_kilde            VARCHAR2(100) AS ('INNTEKTS_KILDE'),
  CONSTRAINT PK_TMP_INNTEKT PRIMARY KEY (id)
);

-- Slutt INNTEKT

-- START -> Ny tabell InntektsPost
CREATE TABLE INNTEKTSPOST (
  id                   NUMBER(19)                        NOT NULL,
  inntekt_id           NUMBER(19)                        NOT NULL,
  inntektspost_type    VARCHAR2(100)                     NOT NULL,
  inntektspost_type_kl VARCHAR2(100) AS ('INNTEKTSPOST_TYPE'),
  fom                  DATE                              NOT NULL,
  tom                  DATE                              NOT NULL,
  beloep               DECIMAL(19, 2)                    NOT NULL,
  versjon              NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av         VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid        TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av            VARCHAR2(20 CHAR),
  endret_tid           TIMESTAMP(3),
  CONSTRAINT PK_INNTEKTSPOST PRIMARY KEY (id),
  CONSTRAINT FK_INNTEKTSPOST_1 FOREIGN KEY (inntekt_id) REFERENCES TMP_INNTEKT
);

CREATE SEQUENCE SEQ_INNTEKTSPOST MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

-- SLUTT INNTEKTSPOST

-- START -> Ny tabell
CREATE TABLE AKTOER_ARBEID (
  id                        NUMBER(19)                        NOT NULL,
  inntekt_arbeid_ytelser_id NUMBER(19)                        NOT NULL,
  aktoer_id                 NUMBER(19)                        NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_AKTOER_ARBEID PRIMARY KEY (id)
  --CONSTRAINT FK_AKTOER_ARBEID_1 FOREIGN KEY (arbeid_opplysninger_id) REFERENCES INNTEKT_ARBEID_YTELSER
);

CREATE SEQUENCE SEQ_AKTOER_ARBEID MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
-- SLUTT AKTOER_ARBEID

-- START -> Ny tabell
CREATE TABLE YRKESAKTIVITET (
  id                         NUMBER(19)                        NOT NULL,
  aktoer_arbeid_id           NUMBER(19)                        NOT NULL,
  arbeidsgiver_aktor_id      VARCHAR2(100),
  arbeidsforhold_id          VARCHAR2(100)                     NOT NULL,
  arbeidsgiver_virksomhet_id NUMBER(19),
  arbeid_type                VARCHAR2(100)                     NOT NULL,
  arbeid_type_kl             VARCHAR2(100) AS ('ARBEID_TYPE'),
  versjon                    NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av               VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid              TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                  VARCHAR2(20 CHAR),
  endret_tid                 TIMESTAMP(3),
  CONSTRAINT PK_YRKESAKTIVITET PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_YRKESAKTIVITET MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
-- SLUTT YRKESAKTIVITET

-- START -> Ny tabell VIRKSOMHET
CREATE TABLE VIRKSOMHET (
  id            NUMBER(19)                        NOT NULL,
  orgnr         VARCHAR2(100)                     NOT NULL,
  navn          VARCHAR2(100)                     NOT NULL,
  registrert    DATE,
  oppstart      DATE,
  avsluttet     DATE,
  versjon       NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av  VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av     VARCHAR2(20 CHAR),
  endret_tid    TIMESTAMP(3),
  CONSTRAINT PK_VIRKSOMHET PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_VIRKSOMHET MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
-- SLUTT VIRKSOMHET PERMISJON

-- START -> Ny tabell PERMISJON
CREATE TABLE PERMISJON (
  id                  NUMBER(19)                        NOT NULL,
  yrkesaktivitet_id   NUMBER(19)                        NOT NULL,
  beskrivelse_type    VARCHAR2(100)                     NOT NULL,
  beskrivelse_type_kl VARCHAR2(100) AS ('PERMISJONSBESKRIVELSE_TYPE'),
  fom                 DATE                              NOT NULL,
  tom                 DATE                              NOT NULL,
  prosentsats         DECIMAL(5, 2)                     NOT NULL,
  versjon             NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av        VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid       TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av           VARCHAR2(20 CHAR),
  endret_tid          TIMESTAMP(3),
  CONSTRAINT PK_PERMISJON PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_PERMISJON MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

-- SLUTT PERMISJON

-- START -> Ny tabell AKTIVITETS_AVTALE
CREATE TABLE AKTIVITETS_AVTALE (
  id                   NUMBER(19)                        NOT NULL,
  yrkesaktivitet_id    NUMBER(19)                        NOT NULL,
  antall_timer         NUMBER(19),
  prosentsats          DECIMAL(5, 2),
  antall_timer_fulltid NUMBER(19),
  fom                  DATE                              NOT NULL,
  tom                  DATE                              NOT NULL,
  versjon              NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av         VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid        TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av            VARCHAR2(20 CHAR),
  endret_tid           TIMESTAMP(3),
  CONSTRAINT PK_AKTIVITETS_AVTALE PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_AKTIVITETS_AVTALE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
-- SLUTT AKTIVITETS_AVTALE


-- KODEVERK --
INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('ARBEID_TYPE', 'N', 'N', 'Arbeid type', 'Kodeverk for arbeid typer');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'VANLIG', 'VANLIG', 'Vanlig', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES
  (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'), 'ARBEID_TYPE');

INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('INNTEKTS_KILDE', 'N', 'N', 'Arbeid type', 'Kodeverk for inntekts kilder');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES
  (seq_kodeliste.nextval, 'INNTEKTS_KILDE', 'VANLIG', 'VANLIG', 'Vanlig', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES
  (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'), 'INNTEKTS_KILDE');

INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('INNTEKTSPOST_TYPE', 'N', 'N', 'Arbeid type', 'Kodeverk for inntektspost type');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES
  (seq_kodeliste.nextval, 'INNTEKTSPOST_TYPE', 'VANLIG', 'VANLIG', 'Vanlig', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'),
        'INNTEKTSPOST_TYPE');

INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('PERMISJONSBESKRIVELSE_TYPE', 'N', 'N', 'Arbeid type', 'Kodeverk for arbeid permisjonsbeskrivelse type');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PERMISJONSBESKRIVELSE_TYPE', 'VANLIG', 'VANLIG', 'Vanlig', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'),
        'PERMISJONSBESKRIVELSE_TYPE');
-- KODEVERK SLUTT --

ALTER TABLE TMP_INNTEKT
  RENAME COLUMN yrkesaktivivitet_id TO yrkesaktivitet_id;
ALTER TABLE TMP_INNTEKT
  ADD opprettet_av VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL;
ALTER TABLE TMP_INNTEKT
  ADD opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL;
ALTER TABLE TMP_INNTEKT
  ADD endret_av VARCHAR2(20 CHAR);
ALTER TABLE TMP_INNTEKT
  ADD endret_tid TIMESTAMP(3);

-- LEGGE TIL CONSTRAINT PÅ BRUK AV KODELISTE

