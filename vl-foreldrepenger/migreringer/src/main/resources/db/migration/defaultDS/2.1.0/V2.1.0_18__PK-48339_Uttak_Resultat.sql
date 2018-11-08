/* POSTERINGSTYPE */
INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('POSTERINGSTYPE', 'N', 'N', 'Posteringstype', 'Internt kodeverk for typen av posteringer.');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PERIODE', 'Periode', 'Periode', 'POSTERINGSTYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'OVERFØRING', 'Overføring', 'Overføring', 'POSTERINGSTYPE', to_date('2000-01-01', 'YYYY-MM-DD'));



INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES ('AVKORTING_AARSAK_TYPE', 'Kodeverk over opphold i uttak.', '', 'VL');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, '-', 'Ikke satt eller valgt kode', 'Ikke satt eller valgt kode',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'AVKORTING_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'SØKT_FOR_SENT', 'Søker har søkt for sent.', 'Søker har søkt for sent.',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'AVKORTING_AARSAK_TYPE');



/* UTTAK_RESULTAT_PLAN */
CREATE TABLE UTTAK_RESULTAT_PLAN (
  ID             NUMBER(19, 0)                                       NOT NULL,
  CONSTRAINT PK_UTTAK_RESULTAT_PLAN PRIMARY KEY (ID)
);

CREATE SEQUENCE SEQ_UTTAL_RESULTAT_PLAN MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE UTTAK_RESULTAT_PLAN IS 'Toppnivå for en uttaksresultatplan.';

/* UTTAK_RES_ARBFHOLD */
CREATE TABLE UTTAK_RESULTAT_ARBFORHOLD (
  ID                        NUMBER(19, 0)                                       NOT NULL,
  ARBEIDSFORHOLD_ID         NUMBER(19,0)                                        NOT NULL,
  UTTAK_RES_PLAN_ID         NUMBER(19, 0)                                       NOT NULL,
  CONSTRAINT PK_UTTAK_RESULTAT_ARBFORHOLD PRIMARY KEY (ID),
  CONSTRAINT FK_UTTAK_RESULTAT_ARBFORHOLD  FOREIGN KEY (UTTAK_RES_PLAN_ID) REFERENCES UTTAK_RESULTAT_PLAN(ID)
);

CREATE SEQUENCE SEQ_UTTAK_RESULTAT_ARBFHOLD MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE UTTAK_RESULTAT_ARBFORHOLD IS 'Angir prosentandel arbeid for dette arbeidsforholdet, og angir om det er en gradering.';
COMMENT ON COLUMN UTTAK_RESULTAT_ARBFORHOLD.ARBEIDSFORHOLD_ID IS 'Id til arbeidsforholdet denne kontoen gjelder for.';
COMMENT ON COLUMN UTTAK_RESULTAT_ARBFORHOLD.UTTAK_RES_PLAN_ID IS 'Fremmednøkkel til uttaksplanen som dette uttaket hører til.';


/* UTTAK_RESULTAT_PERIODE */
CREATE TABLE UTTAK_RESULTAT_PERIODE (
  ID                              NUMBER(19, 0)                                       NOT NULL,
  UTTAK_RESULTAT_ARBFORHOLD_ID    NUMBER(19, 0)                                       NOT NULL,
  TREKKDAGER                      NUMBER(3,0)                                         NOT NULL,
  STOENADSKONTOTYPE               VARCHAR2(100 CHAR)                                  NOT NULL,
  AARSAK_TYPE                     VARCHAR2(100)                                       NOT NULL,
  KL_AARSAK_TYPE                  VARCHAR2(100)                                       NOT NULL,
  AVKORTING_AARSAK_TYPE           VARCHAR2(100 CHAR)                                  NOT NULL,
  UTTAK_PERIODE_TYPE              VARCHAR2(100 CHAR)                                  NOT NULL,
  GRADERING                       CHAR(1)                                             NOT NULL,
  PROSENT_ARBEID                  NUMBER(3,0)                                         NOT NULL,
  REGEL_INPUT                     CLOB                                                NOT NULL,
  REGEL_EVALUERING                CLOB                                                NOT NULL,
  FOM                             TIMESTAMP(3)                                        NOT NULL,
  TOM                             TIMESTAMP(3)                                        NOT NULL,

  CONSTRAINT PK_UTTAK_RESULTAT_PERIODE PRIMARY KEY (ID),
  CONSTRAINT FK_UTTAK_RESULTAT_PERIODE_1 FOREIGN KEY (UTTAK_RESULTAT_ARBFORHOLD_ID) REFERENCES UTTAK_RESULTAT_ARBFORHOLD(ID),
  CONSTRAINT FK_UTTAK_RESULTAT_PERIODE_2 FOREIGN KEY (KL_AARSAK_TYPE, AARSAK_TYPE) REFERENCES KODELISTE(kodeverk, kode)
);

CREATE SEQUENCE SEQ_UTTAK_RESULTAT_PERIODE MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE UTTAK_RESULTAT_PERIODE IS 'Angir prosentandel arbeid for dette arbeidsforholdet, og angir om det er en gradering.';
COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE.TREKKDAGER IS 'Antall virkedager som skal trekkes. Kan avvike fra periode.';
COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE.STOENADSKONTOTYPE IS 'Hvilken stønadskonto det skal trekkes fra.';
COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE.AVKORTING_AARSAK_TYPE IS 'Årsak til avkorting.';
COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE.GRADERING IS '''J'' dersom bruker har en gradering ifm dette arbeidsforholdet, ellers ''N''.';
COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE.PROSENT_ARBEID IS 'Hvor mange prosent bruker ønsker å arbeide i dette arbeidsforholdet.';
COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE.REGEL_INPUT IS 'JSON representasjon av input';
COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE.REGEL_EVALUERING IS 'JSON representasjon av evalurering';
COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE.UTTAK_RESULTAT_ARBFORHOLD_ID IS 'Fremmednøkkel til resultatet for arbeidsforholdet denne perioden hører til.';
COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE.FOM IS 'Fra-og-med dato for uttaket.';
COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE.TOM IS 'Til-og-med dato for uttaket.';


/* UTTAK_POSTERINGER */
CREATE TABLE UTTAK_POSTERINGER (
  ID                        NUMBER(19, 0)                                       NOT NULL,
  ARBEIDSFORHOLD_ID         NUMBER(19, 0)                                       NOT NULL,
  UTTAK_RESULTAT_PERIODE_ID NUMBER(19, 0)                                       NOT NULL,
  STOENADSKONTO_ID          NUMBER(19, 0)                                       NOT NULL,
  BEHANDLING_ID             NUMBER(19, 0)                                       NOT NULL,
  GRADERING_PROSENT         NUMBER(3,0)                                         NOT NULL,
  POSTERINGSTYPE            VARCHAR2(100 CHAR)                                  NOT NULL,
  VIRKEDAGER_BRUKT          NUMBER(3,0)                                         NOT NULL,

  CONSTRAINT PK_UTTAK_POSTERINGER PRIMARY KEY (ID),
  CONSTRAINT FK_UTTAK_POSTERINGER_1 FOREIGN KEY (UTTAK_RESULTAT_PERIODE_ID) REFERENCES UTTAK_RESULTAT_PERIODE(ID),
  CONSTRAINT FK_UTTAK_POSTERINGER_2 FOREIGN KEY (STOENADSKONTO_ID) REFERENCES STOENADSKONTO(ID),
  CONSTRAINT FK_UTTAK_POSTERINGER_3 FOREIGN KEY (BEHANDLING_ID) REFERENCES BEHANDLING(ID)

);

CREATE SEQUENCE SEQ_UTTAK_POSTERINGER MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE UTTAK_POSTERINGER IS 'Posteringer koblet til en stønadskonto.';
COMMENT ON COLUMN UTTAK_POSTERINGER.VIRKEDAGER_BRUKT IS 'Antall virkedager brukt ifm uttaket.';
COMMENT ON COLUMN UTTAK_POSTERINGER.UTTAK_RESULTAT_PERIODE_ID IS 'Fremmednøkkel til perioden denne posteringen hører til.';




