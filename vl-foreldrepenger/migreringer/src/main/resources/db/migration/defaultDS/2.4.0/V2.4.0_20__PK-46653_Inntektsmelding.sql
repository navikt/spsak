ALTER TABLE GR_ARBEID_INNTEKT
  ADD inntektsmeldinger_id NUMBER(19);

-- Opprett tabeller
CREATE TABLE INNTEKTSMELDINGER (
  id            NUMBER(19)                        NOT NULL,
  versjon       NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av  VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av     VARCHAR2(20 CHAR),
  endret_tid    TIMESTAMP(3),
  CONSTRAINT PK_INNTEKTSMELDINGER PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_INNTEKTSMELDINGER
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NOCACHE
  NOCYCLE;

CREATE TABLE INNTEKTSMELDING (
  id                   NUMBER(19)                        NOT NULL,
  inntektsmeldinger_id NUMBER(19)                        NOT NULL,
  mottatt_dokument_id  NUMBER(19)                        NOT NULL,
  versjon              NUMBER(19) DEFAULT 0              NOT NULL,
  virksomhet_id        NUMBER(19)                        NOT NULL,
  arbeidsforhold_id    VARCHAR2(200 CHAR),
  inntekt_beloep       NUMBER(10, 2)                     NOT NULL,
  start_dato_permisjon DATE                              NOT NULL,
  refusjon_beloep      NUMBER(10, 2),
  refusjon_opphoerer   DATE,
  naer_relasjon        VARCHAR2(1 CHAR)                  NOT NULL,
  opprettet_av         VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid        TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av            VARCHAR2(20 CHAR),
  endret_tid           TIMESTAMP(3),
  CONSTRAINT PK_INNTEKTSMELDING PRIMARY KEY (id),
  CONSTRAINT FK_INNTEKTSMELDING_1 FOREIGN KEY (inntektsmeldinger_id) REFERENCES INNTEKTSMELDINGER,
  CONSTRAINT FK_INNTEKTSMELDING_2 FOREIGN KEY (virksomhet_id) REFERENCES VIRKSOMHET,
  CONSTRAINT FK_INNTEKTSMELDING_3 FOREIGN KEY (mottatt_dokument_id) REFERENCES MOTTATT_DOKUMENT,
  CONSTRAINT CHK_FK_INNTEKTSMELDING_1 CHECK (naer_relasjon IN ('J', 'N'))
);

CREATE SEQUENCE SEQ_INNTEKTSMELDING
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NOCACHE
  NOCYCLE;

CREATE TABLE NATURAL_YTELSE (
  id                     NUMBER(19)                        NOT NULL,
  inntektsmelding_id     NUMBER(19)                        NOT NULL,
  natural_ytelse_type    VARCHAR2(100 CHAR)                NOT NULL,
  kl_natural_ytelse_type VARCHAR2(100) AS ('NATURAL_YTELSE_TYPE'),
  beloep_mnd             NUMBER(10, 2)                     NOT NULL,
  fom                    DATE                              NOT NULL,
  tom                    DATE                              NOT NULL,
  versjon                NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av           VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid          TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av              VARCHAR2(20 CHAR),
  endret_tid             TIMESTAMP(3),
  CONSTRAINT PK_NATURAL_YTELSE PRIMARY KEY (id),
  CONSTRAINT FK_NATURAL_YTELSE_1 FOREIGN KEY (inntektsmelding_id) REFERENCES INNTEKTSMELDING,
  CONSTRAINT FK_NATURAL_YTELSE_2 FOREIGN KEY (kl_natural_ytelse_type, natural_ytelse_type) REFERENCES KODELISTE (kodeverk, kode)
);

CREATE SEQUENCE SEQ_NATURAL_YTELSE
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NOCACHE
  NOCYCLE;

CREATE TABLE GRADERING (
  id                 NUMBER(19)                        NOT NULL,
  inntektsmelding_id NUMBER(19)                        NOT NULL,
  arbeidstid_prosent NUMBER(5, 2)                      NOT NULL,
  fom                DATE                              NOT NULL,
  tom                DATE                              NOT NULL,
  versjon            NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av       VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid      TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av          VARCHAR2(20 CHAR),
  endret_tid         TIMESTAMP(3),
  CONSTRAINT PK_GRADERING PRIMARY KEY (id),
  CONSTRAINT FK_GRADERING_1 FOREIGN KEY (inntektsmelding_id) REFERENCES INNTEKTSMELDING
);

CREATE SEQUENCE SEQ_GRADERING
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NOCACHE
  NOCYCLE;

CREATE TABLE UTSETTELSE_PERIODE (
  id                         NUMBER(19)                        NOT NULL,
  inntektsmelding_id         NUMBER(19)                        NOT NULL,
  utsettelse_periode_type    VARCHAR2(100 CHAR)                NOT NULL,
  kl_utsettelse_periode_type VARCHAR2(100) AS ('UTSETTELSE_PERIODE_TYPE'),
  utsettelse_aarsak_type     VARCHAR2(100 CHAR)                NOT NULL,
  kl_utsettelse_aarsak_type  VARCHAR2(100) AS ('UTSETTELSE_AARSAK_TYPE'),
  fom                        DATE                              NOT NULL,
  tom                        DATE                              NOT NULL,
  versjon                    NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av               VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid              TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                  VARCHAR2(20 CHAR),
  endret_tid                 TIMESTAMP(3),
  CONSTRAINT PK_UTSETTELSE_PERIODE PRIMARY KEY (id),
  CONSTRAINT FK_UTSETTELSE_PERIODE_1 FOREIGN KEY (inntektsmelding_id) REFERENCES INNTEKTSMELDING,
  CONSTRAINT FK_UTSETTELSE_PERIODE_2 FOREIGN KEY (kl_utsettelse_periode_type, utsettelse_periode_type) REFERENCES KODELISTE (kodeverk, kode),
  CONSTRAINT FK_UTSETTELSE_PERIODE_3 FOREIGN KEY (kl_utsettelse_aarsak_type, utsettelse_aarsak_type) REFERENCES KODELISTE (kodeverk, kode)
);

CREATE SEQUENCE SEQ_UTSETTELSE_PERIODE
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NOCACHE
  NOCYCLE;

-- Kodeverk && kodeverksverdier
INSERT INTO KODEVERK (KODE, NAVN, BESKRIVELSE) VALUES
  ('NATURAL_YTELSE_TYPE', 'Natural ytelse typer', 'Forskjellige former for natural ytelser fra inntektsmeldingen.');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'ELEKTRISK_KOMMUNIKASJON', 'elektroniskKommunikasjon',
   'Elektrisk kommunikasjon', 'Elektrisk kommunikasjon', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'AKSJER_UNDERKURS', 'aksjerGrunnfondsbevisTilUnderkurs',
   'Aksjer grunnfondsbevis til underkurs', 'Aksjer grunnfondsbevis til underkurs', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'LOSJI', 'losji', 'Losji', 'Losji',
   to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'KOST_DOEGN', 'kostDoegn', 'Kostpenger døgnsats',
   'Kostpenger døgnsats', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'BESOEKSREISER_HJEM', 'besoeksreiserHjemmetAnnet',
   'Besøksreiser hjemmet annet', 'Besøksreiser hjemmet annet', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'KOSTBESPARELSE_HJEM', 'kostbesparelseIHjemmet',
   'Kostbesparelser i hjemmet', 'Kostbesparelser i hjemmet', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'RENTEFORDEL_LAAN', 'rentefordelLaan', 'Rentefordel lån',
   'Rentefordel lån', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'BIL', 'bil', 'Bil', 'Bil', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'KOST_DAGER', 'kostDager', 'Kostpenger dager', 'Kostpenger dager',
   to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'BOLIG', 'bolig', 'Bolig', 'Bolig',
   to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'FORSIKRINGER', 'skattepliktigDelForsikringer',
   'Skattepliktig del forsikringer', 'Skattepliktig del forsikringer', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'FRI_TRANSPORT', 'friTransport', 'Fri transport', 'Fri transport',
   to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'OPSJONER', 'opsjoner', 'Opsjoner', 'Opsjoner',
   to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'TILSKUDD_BARNEHAGE', 'tilskuddBarnehageplass',
   'Tilskudd barnehageplass', 'Tilskudd barnehageplass', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'ANNET', 'annet', 'Annet', 'Annet',
   to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'BEDRIFTSBARNEHAGE', 'bedriftsbarnehageplass',
   'Bedriftsbarnehageplass', 'Bedriftsbarnehageplass', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'YRKESBIL_KILOMETER', 'yrkebilTjenestligbehovKilometer',
   'Yrkesbil tjenesteligbehov kilometer', 'Yrkesbil tjenesteligbehov kilometer', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'YRKESBIL_LISTEPRIS', 'yrkebilTjenestligbehovListepris',
   'Yrkesbil tjenesteligbehov listepris', 'Yrkesbil tjenesteligbehov listepris', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'NATURAL_YTELSE_TYPE', 'UTENLANDSK_PENSJONSORDNING',
   'innbetalingTilUtenlandskPensjonsordning', 'Innbetaling utenlandsk pensjonsordning',
   'Innbetaling utenlandsk pensjonsordning', to_date('2006-07-01', 'YYYY-MM-DD'));

INSERT INTO KODEVERK (KODE, NAVN, BESKRIVELSE) VALUES ('UTSETTELSE_PERIODE_TYPE', 'Utsettelse periode type',
                                                       'Forskjellige typer utsetteser fra arbeidsgiver i innteksmeldingen.');
INSERT INTO KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK)
VALUES (seq_kodeliste.nextval, 'FERIE', 'Ferie', 'Ferie', 'UTSETTELSE_PERIODE_TYPE');
INSERT INTO KODELISTE (ID, KODE, NAVN, BESKRIVELSE, KODEVERK)
VALUES (seq_kodeliste.nextval, 'UTSETTELSE', 'Utsettelse', 'Utsettelse', 'UTSETTELSE_PERIODE_TYPE');

INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_kodeliste.nextval, 'DOKUMENT_TYPE_ID', 'INNTEKTSMELDING', 'I000067',
                               'Opplysninger for å behandle krav om blant annet foreldrepenger',
                               NULL, 'NB', to_date('01.12.2017', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'),
                               'VL', to_timestamp('10.11.2017', 'DD.MM.RRRR'));

UPDATE KODELISTE
SET kode = 'LOVBESTEMT_FERIE'
WHERE KODEVERK = 'UTSETTELSE_AARSAK_TYPE' AND KODE = 'FERIE';

ALTER TABLE GR_ARBEID_INNTEKT MODIFY INNTEKT_ARBEID_YTELSER_ID NULL;
