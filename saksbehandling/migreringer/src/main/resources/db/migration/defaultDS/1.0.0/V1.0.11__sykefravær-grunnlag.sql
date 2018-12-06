CREATE TABLE SF_SYKEMELDINGER (
  id            bigint                        NOT NULL,
  versjon       bigint DEFAULT 0              NOT NULL,
  opprettet_av  VARCHAR(20) DEFAULT 'VL'    NOT NULL,
  opprettet_tid TIMESTAMP(3) DEFAULT localtimestamp NOT NULL,
  endret_av     VARCHAR(20),
  endret_tid    TIMESTAMP(3),
  CONSTRAINT PK_SF_SYKEMELDINGER PRIMARY KEY (id)
);
COMMENT ON TABLE SF_SYKEMELDINGER
IS 'Mange-til-mange tabell mellom grunnlag og sykemeldinger';

CREATE SEQUENCE SEQ_SF_SYKEMELDINGER
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50

  NO CYCLE;

CREATE TABLE SF_SYKEFRAVAER (
  id            bigint                        NOT NULL,
  versjon       bigint DEFAULT 0              NOT NULL,
  opprettet_av  VARCHAR(20) DEFAULT 'VL'    NOT NULL,
  opprettet_tid TIMESTAMP(3) DEFAULT localtimestamp NOT NULL,
  endret_av     VARCHAR(20),
  endret_tid    TIMESTAMP(3),
  CONSTRAINT PK_SF_SYKEFRAVAER PRIMARY KEY (id)
);

COMMENT ON TABLE SF_SYKEFRAVAER
IS 'Mange-til-mange tabell mellom grunnlag og sykefraværs-perioder';

CREATE SEQUENCE SEQ_SF_SYKEFRAVAER
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50

  NO CYCLE;

CREATE TABLE GR_SYKEFRAVAER (
  id               bigint                        NOT NULL,
  behandling_id    bigint                        NOT NULL,
  sykemeldinger_id bigint,
  sykefravaer_id   bigint,
  aktiv            VARCHAR(1) DEFAULT 'N'      NOT NULL,
  versjon          bigint DEFAULT 0              NOT NULL,
  opprettet_av     VARCHAR(20) DEFAULT 'VL'    NOT NULL,
  opprettet_tid    TIMESTAMP(3) DEFAULT localtimestamp NOT NULL,
  endret_av        VARCHAR(20),
  endret_tid       TIMESTAMP(3),
  CONSTRAINT PK_GR_SYKEFRAVAER PRIMARY KEY (id),
  CONSTRAINT FK_GR_SYKEFRAVAER_1 FOREIGN KEY (behandling_id) REFERENCES BEHANDLING,
  CONSTRAINT FK_GR_SYKEFRAVAER_2 FOREIGN KEY (sykemeldinger_id) REFERENCES SF_SYKEMELDINGER,
  CONSTRAINT FK_GR_SYKEFRAVAER_3 FOREIGN KEY (sykefravaer_id) REFERENCES SF_SYKEFRAVAER,
  CONSTRAINT CHK_GR_SYKEFRAVAER CHECK (AKTIV IN ('J', 'N'))
);

COMMENT ON TABLE GR_SYKEFRAVAER
IS 'Grunnlaget for sykemeldinger og sykefraværs-perioder';

CREATE SEQUENCE SEQ_GR_SYKEFRAVAER
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50

  NO CYCLE;

CREATE INDEX IDX_GR_SYKEFRAVAER_01
  ON GR_SYKEFRAVAER (behandling_id);
CREATE INDEX IDX_GR_SYKEFRAVAER_02
  ON GR_SYKEFRAVAER (sykefravaer_id);
CREATE INDEX IDX_GR_SYKEFRAVAER_03
  ON GR_SYKEFRAVAER (sykemeldinger_id);

CREATE UNIQUE INDEX UIDX_GR_SYKEFRAVAER_01
  ON GR_SYKEFRAVAER (
    (CASE WHEN AKTIV = 'J'
      THEN BEHANDLING_ID
     ELSE NULL END),
    (CASE WHEN AKTIV = 'J'
      THEN AKTIV
     ELSE NULL END)
  );

CREATE TABLE SF_SYKEMELDING (
  id                         bigint                        NOT NULL,
  versjon                    bigint DEFAULT 0              NOT NULL,
  sykemeldinger_id           bigint                        NOT NULL,
  EKSTERN_REFERANSE          VARCHAR(100)                NOT NULL,
  ARBEIDSGIVER_AKTOR_ID      VARCHAR(100),
  ARBEIDSGIVER_VIRKSOMHET_ID bigint,
  GRAD                       NUMERIC(5, 2)                      NOT NULL,
  FOM                        DATE                              NOT NULL,
  TOM                        DATE                              NOT NULL,
  opprettet_av               VARCHAR(20) DEFAULT 'VL'    NOT NULL,
  opprettet_tid              TIMESTAMP(3) DEFAULT localtimestamp NOT NULL,
  endret_av                  VARCHAR(20),
  endret_tid                 TIMESTAMP(3),
  CONSTRAINT PK_SF_SYKEMELDING PRIMARY KEY (id),
  CONSTRAINT FK_SF_SYKEMELDING_1 FOREIGN KEY (sykemeldinger_id) REFERENCES SF_SYKEMELDINGER,
  CONSTRAINT FK_SF_SYKEMELDING_2 FOREIGN KEY (ARBEIDSGIVER_VIRKSOMHET_ID) REFERENCES VIRKSOMHET
);

CREATE SEQUENCE SEQ_SF_SYKEMELDING
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50

  NO CYCLE;

CREATE INDEX IDX_SF_SYKEMELDING_01
  ON SF_SYKEMELDING (sykemeldinger_id);
CREATE INDEX IDX_SF_SYKEMELDING_02
  ON SF_SYKEMELDING (ARBEIDSGIVER_VIRKSOMHET_ID);

COMMENT ON TABLE SF_SYKEMELDING
IS 'Sykemeldingen';
COMMENT ON COLUMN SF_SYKEMELDING.GRAD
IS 'Graden av sykemelding (prosentsats)';
COMMENT ON COLUMN SF_SYKEMELDING.FOM
IS 'Fra og med dato for sykemeldingen';
COMMENT ON COLUMN SF_SYKEMELDING.FOM
IS 'Til og med dato for sykemeldingen';
COMMENT ON COLUMN SF_SYKEMELDING.ARBEIDSGIVER_AKTOR_ID
IS 'Arbeidsgivers aktørId hvis personlig foretak';
COMMENT ON COLUMN SF_SYKEMELDING.EKSTERN_REFERANSE
IS 'Sykemeldingen eksterne referanse til sykemeldingsregisteret';

CREATE TABLE SF_SYKEFRAVAER_PERIODE (
  id                         bigint                        NOT NULL,
  versjon                    bigint DEFAULT 0              NOT NULL,
  sykefravaer_id             bigint                        NOT NULL,
  ARBEIDSGIVER_AKTOR_ID      VARCHAR(100),
  ARBEIDSGIVER_VIRKSOMHET_ID bigint,
  gradering                  NUMERIC(5, 2)                      NOT NULL,
  arbeidsgrad                NUMERIC(5, 2)                      NOT NULL,
  FOM                        DATE                              NOT NULL,
  TOM                        DATE                              NOT NULL,
  fravaer_type               VARCHAR(100)                NOT NULL,
  kl_fravaer_type            VARCHAR(100) DEFAULT 'SYKEFRAVÆR_PERIODE_TYPE',
  opprettet_av               VARCHAR(20) DEFAULT 'VL'    NOT NULL,
  opprettet_tid              TIMESTAMP(3) DEFAULT localtimestamp NOT NULL,
  endret_av                  VARCHAR(20),
  endret_tid                 TIMESTAMP(3),
  CONSTRAINT PK_SF_SYKEFRAVAER_PERIODE PRIMARY KEY (id),
  CONSTRAINT FK_SF_SYKEFRAVAER_PERIODE_1 FOREIGN KEY (sykefravaer_id) REFERENCES SF_SYKEFRAVAER,
  CONSTRAINT FK_SF_SYKEFRAVAER_PERIODE_2 FOREIGN KEY (ARBEIDSGIVER_VIRKSOMHET_ID) REFERENCES VIRKSOMHET,
  CONSTRAINT FK_SF_SYKEFRAVAER_PERIODE_3 FOREIGN KEY (kl_fravaer_type, fravaer_type) REFERENCES KODELISTE (kodeverk, kode)
);

CREATE SEQUENCE SEQ_SF_SYKEFRAVAER_PERIODE
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50

  NO CYCLE;

CREATE INDEX IDX_SF_SYKEFRAVAER_PERIODE_01
  ON SF_SYKEFRAVAER_PERIODE (sykefravaer_id);
CREATE INDEX IDX_SF_SYKEFRAVAER_PERIODE_02
  ON SF_SYKEFRAVAER_PERIODE (ARBEIDSGIVER_VIRKSOMHET_ID);
CREATE INDEX IDX_SF_SYKEFRAVAER_PERIODE_03
  ON SF_SYKEFRAVAER_PERIODE (fravaer_type);

COMMENT ON TABLE SF_SYKEFRAVAER_PERIODE
IS 'Sykemeldingen';
COMMENT ON COLUMN SF_SYKEFRAVAER_PERIODE.arbeidsgrad
IS 'Graden av arbeids i perioden (prosentsats)';
COMMENT ON COLUMN SF_SYKEFRAVAER_PERIODE.gradering
IS 'Graden av gradering i perioden (prosentsats)';
COMMENT ON COLUMN SF_SYKEFRAVAER_PERIODE.FOM
IS 'Fra og med dato';
COMMENT ON COLUMN SF_SYKEFRAVAER_PERIODE.FOM
IS 'Til og med dato';
COMMENT ON COLUMN SF_SYKEFRAVAER_PERIODE.ARBEIDSGIVER_AKTOR_ID
IS 'Arbeidsgivers aktørId hvis personlig foretak';
