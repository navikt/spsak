CREATE TABLE YF_DOKUMENTASJON_PERIODER (
  id                      NUMBER(19)                                                             NOT NULL,
  dokumentasjon_klasse    VARCHAR2(100 CHAR)                                                     NOT NULL,
  kl_dokumentasjon_klasse VARCHAR2(100 CHAR) DEFAULT 'UTTAK_DOKUMENTASJON_KLASSE'                NOT NULL,
  versjon                 NUMBER(19) DEFAULT 0                                                   NOT NULL,
  opprettet_av            VARCHAR2(20 CHAR) DEFAULT 'VL'                                         NOT NULL,
  opprettet_tid           TIMESTAMP(3) DEFAULT systimestamp                                      NOT NULL,
  endret_av               VARCHAR2(20 CHAR),
  endret_tid              TIMESTAMP(3),
  CONSTRAINT PK_YF_DOKUMENTASJON_PERIODER PRIMARY KEY (id),
  CONSTRAINT FK_YF_DOKUMENTASJON_PERIODER_1 FOREIGN KEY (kl_dokumentasjon_klasse, dokumentasjon_klasse) REFERENCES KODELISTE (KODEVERK, KODE)
);

CREATE TABLE YF_DOKUMENTASJON_PERIODE (
  id                      NUMBER(19)                                                             NOT NULL,
  perioder_id             NUMBER(19)                                                             NOT NULL,
  fom                     DATE                                                                   NOT NULL,
  tom                     DATE                                                                   NOT NULL,
  dokumentasjon_type      VARCHAR2(100 CHAR)                                                     NOT NULL,
  kl_dokumentasjon_type   VARCHAR2(100 CHAR) DEFAULT 'UTTAK_DOKUMENTASJON_TYPE'                  NOT NULL,
  dokumentasjon_klasse    VARCHAR2(100 CHAR)                                                     NOT NULL,
  kl_dokumentasjon_klasse VARCHAR2(100 CHAR) DEFAULT 'UTTAK_DOKUMENTASJON_KLASSE'                NOT NULL,
  opprettet_av            VARCHAR2(20 CHAR) DEFAULT 'VL'                                         NOT NULL,
  opprettet_tid           TIMESTAMP(3) DEFAULT systimestamp                                      NOT NULL,
  endret_av               VARCHAR2(20 CHAR),
  endret_tid              TIMESTAMP(3),
  CONSTRAINT PK_YF_DOKUMENTASJON_PERIODE PRIMARY KEY (id),
  CONSTRAINT FK_YF_DOKUMENTASJON_PERIODE_1 FOREIGN KEY (perioder_id) REFERENCES YF_DOKUMENTASJON_PERIODER (ID),
  CONSTRAINT FK_YF_DOKUMENTASJON_PERIODE_2 FOREIGN KEY (kl_dokumentasjon_type, dokumentasjon_type) REFERENCES KODELISTE (KODEVERK, KODE),
  CONSTRAINT FK_YF_DOKUMENTASJON_PERIODE_3 FOREIGN KEY (kl_dokumentasjon_klasse, dokumentasjon_klasse) REFERENCES KODELISTE (KODEVERK, KODE)
);

CREATE INDEX IDX_YF_DOKUMENTASJON_PERIODE_1
  ON YF_DOKUMENTASJON_PERIODE (perioder_id);

CREATE SEQUENCE SEQ_YF_DOKUMENTASJON_PERIODE MINVALUE 1 START WITH 100000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_YF_DOKUMENTASJON_PERIODER MINVALUE 1 START WITH 100000 INCREMENT BY 50 NOCACHE NOCYCLE;

INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('UTTAK_DOKUMENTASJON_TYPE', 'N', 'N', 'Dokumentasjonstype for søknadsperioder', 'Dokumentasjonstype for søknadsperioder');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UTTAK_DOKUMENTASJON_TYPE', 'UTEN_OMSORG', 'Søker har ikke omsorg for barnet',
        'Søker har ikke omsorg for barnet', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UTTAK_DOKUMENTASJON_TYPE', 'ALENEOMSORG', 'Søker har aleneomsorg for barnet',
        'Søker har aleneomsorg for barnet', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UTTAK_DOKUMENTASJON_TYPE', 'INNLAGT_BARN', 'Barn er innlagt i institusjon',
        'Det er dokumentert at barn er innlagt', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UTTAK_DOKUMENTASJON_TYPE', 'INNLAGT_SOKER', 'Søker er innlagt i institusjon',
        'Det er dokumentert at søker er innlagt', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UTTAK_DOKUMENTASJON_TYPE', 'SYK_SOKER', 'Søker er syk eller skadet',
        'Det er dokumentert at søker er syk eller skadet', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UTTAK_DOKUMENTASJON_TYPE', 'UTEN_DOKUMENTASJON', 'Søkt periode er ikke dokumentert',
        'Søkt periode er ikke dokumentert', to_date('2000-01-01', 'YYYY-MM-DD'));


INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('UTTAK_DOKUMENTASJON_KLASSE', 'N', 'N', 'Dokumentasjonsklasse for søknadsperioder',
        'Dokumentasjonsklasse for søknadsperioder - brukes av hibernate for å velge riktig implementasjon');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UTTAK_DOKUMENTASJON_KLASSE', 'UTEN_OMSORG', 'Søker har ikke omsorg for barnet',
        'Søker har ikke omsorg for barnet', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UTTAK_DOKUMENTASJON_KLASSE', 'ALENEOMSORG', 'Søker har aleneomsorg for barnet',
        'Søker har aleneomsorg for barnet', to_date('2000-01-01', 'YYYY-MM-DD'));


ALTER TABLE GR_YTELSES_FORDELING
  ADD (
  utenomsorg_id NUMBER,
  aleneomsorg_id NUMBER
  );


ALTER TABLE GR_YTELSES_FORDELING
  ADD CONSTRAINT FK_YF_DOKUMENTASJON_PERIODE_9 FOREIGN KEY (utenomsorg_id) REFERENCES YF_DOKUMENTASJON_PERIODER (ID);
ALTER TABLE GR_YTELSES_FORDELING
  ADD CONSTRAINT FK_YF_DOKUMENTASJON_PERIODE_10 FOREIGN KEY (aleneomsorg_id) REFERENCES YF_DOKUMENTASJON_PERIODER (ID);
