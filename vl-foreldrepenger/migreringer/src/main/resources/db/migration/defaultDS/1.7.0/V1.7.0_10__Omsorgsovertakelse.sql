-- Tabell: OMSORGSOVERTAKELSE_VILKAR_TYPE
CREATE TABLE OMSORGSOVERTAKELSE_VILKAR_TYPE (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_OMSORG_VILKAR_TYPE PRIMARY KEY (kode)
);

COMMENT ON TABLE OMSORGSOVERTAKELSE_VILKAR_TYPE  IS 'Kodeverk for vilkår for omsorgsovertakelse';
COMMENT ON COLUMN OMSORGSOVERTAKELSE_VILKAR_TYPE.KODE IS 'Kodeverk Primary Key';
COMMENT ON COLUMN OMSORGSOVERTAKELSE_VILKAR_TYPE.BESKRIVELSE IS 'Utdypende beskrivelse av koden';

INSERT INTO OMSORGSOVERTAKELSE_VILKAR_TYPE(kode, navn) VALUES ('OMSORGSOVERTAKELSE', 'Omsorgsvilkår §14-17 tredje ledd');
INSERT INTO OMSORGSOVERTAKELSE_VILKAR_TYPE(kode, navn) VALUES ('FORELDREANSVAR_1', 'Foreldreansvarsvilkåret §14-17 andre ledd');
INSERT INTO OMSORGSOVERTAKELSE_VILKAR_TYPE(kode, navn) VALUES ('FORELDREANSVAR_2', 'Foreldreansvarsvilkåret §14-17 fjerde ledd');


-- Tabell: OMSORGSOVERTAKELSE
CREATE TABLE OMSORGSOVERTAKELSE (
  id                             NUMBER(19) NOT NULL,
  behandling_grunnlag_id         NUMBER(19) NOT NULL,
  omsorgsovertakelsedato         DATE,
  vilkaarstype                   VARCHAR2(50 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_OMSORGSOVERTAKELSE PRIMARY KEY (id),
  CONSTRAINT FK_OMSORGSOVERTAKELSE_1 FOREIGN KEY (vilkaarstype) REFERENCES OMSORGSOVERTAKELSE_VILKAR_TYPE,
  CONSTRAINT FK_OMSORGSOVERTAKELSE_2 FOREIGN KEY (behandling_grunnlag_id) REFERENCES BEHANDLING_GRUNNLAG
);

CREATE INDEX IDX_OMSORGSOVERTAKELSE_1 ON OMSORGSOVERTAKELSE(vilkaarstype);
CREATE INDEX IDX_OMSORGSOVERTAKELSE_2 ON OMSORGSOVERTAKELSE(behandling_grunnlag_id);
CREATE SEQUENCE SEQ_OMSORGSOVERTAKELSE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE OMSORGSOVERTAKELSE IS 'Informasjon om omsorgsovertakelse';
COMMENT ON COLUMN OMSORGSOVERTAKELSE.OMSORGSOVERTAKELSEDATO IS 'Dato for omsorgsovertakelse';


-- Tabell: BEKREFTET_BARN
CREATE TABLE BEKREFTET_BARN (
  id                             NUMBER(19) NOT NULL,
  behandling_grunnlag_id         NUMBER(19) NOT NULL,
  aktor_id                       NUMBER(19),
  navn                           VARCHAR2(100),
  fødselsdato                    DATE NOT NULL,
  adresse                        VARCHAR2(500) NOT NULL,
  bekreftet_via_tps              VARCHAR2(1) DEFAULT 'N',
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_BEKREFTET_BARN PRIMARY KEY (id),
  CONSTRAINT FK_BEKREFTET_BARN_1 FOREIGN KEY (behandling_grunnlag_id) REFERENCES BEHANDLING_GRUNNLAG
);

CREATE INDEX IDX_BEKREFTET_BARN_1 ON BEKREFTET_BARN(behandling_grunnlag_id);
CREATE SEQUENCE SEQ_BEKREFTET_BARN MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE BEKREFTET_BARN IS 'Bekreftede opplysninger om barn';
COMMENT ON COLUMN BEKREFTET_BARN.aktor_id IS 'Aktør id';
COMMENT ON COLUMN BEKREFTET_BARN.navn IS 'Navn på barn';
COMMENT ON COLUMN BEKREFTET_BARN.fødselsdato IS 'Fødselsdato';
COMMENT ON COLUMN BEKREFTET_BARN.adresse IS 'Folkeregistrert adresse på barn';
COMMENT ON COLUMN BEKREFTET_BARN.bekreftet_via_tps IS 'Bekreftet via TPS';


-- Tabell: BEKREFTET_FORELDRE
CREATE TABLE BEKREFTET_FORELDRE (
  id                             NUMBER(19) NOT NULL,
  behandling_grunnlag_id         NUMBER(19) NOT NULL,
  aktor_id                       NUMBER(19),
  navn                           VARCHAR2(100),
  dødsdato                       DATE,
  adresse                        VARCHAR2(500) NOT NULL,
  bekreftet_via_tps              VARCHAR2(1) DEFAULT 'N',
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_BEKREFTET_FORELDRE PRIMARY KEY (id),
  CONSTRAINT FK_BEKREFTET_FORELDRE_1 FOREIGN KEY (behandling_grunnlag_id) REFERENCES BEHANDLING_GRUNNLAG
);

CREATE INDEX IDX_BEKREFTET_FORELDRE_1 ON BEKREFTET_FORELDRE(behandling_grunnlag_id);
CREATE SEQUENCE SEQ_BEKREFTET_FORELDRE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE BEKREFTET_FORELDRE IS 'Bekreftede opplysninger om foreldre';
COMMENT ON COLUMN BEKREFTET_FORELDRE.aktor_id IS 'Aktør id';
COMMENT ON COLUMN BEKREFTET_FORELDRE.navn IS 'Navn på forelder';
COMMENT ON COLUMN BEKREFTET_FORELDRE.dødsdato IS 'Dødsdato';
COMMENT ON COLUMN BEKREFTET_FORELDRE.adresse IS 'Folkeregistrert adresse på forelder';
COMMENT ON COLUMN BEKREFTET_FORELDRE.bekreftet_via_tps IS 'Bekreftet via TPS';


-- Tabell: AVSLAGSARSAK
CREATE TABLE AVSLAGSARSAK (
  avslag_kode                    VARCHAR2(20 CHAR) NOT NULL,
  vilkar_kode                    VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_AVSLAGSARSAK PRIMARY KEY (avslag_kode, vilkar_kode),
  CONSTRAINT FK_AVSLAGSARSAK FOREIGN KEY (vilkar_kode) REFERENCES VILKAR_TYPE
);

COMMENT ON TABLE AVSLAGSARSAK  IS 'Avslagsårsak';
COMMENT ON COLUMN AVSLAGSARSAK.avslag_kode IS 'Kode for avslagsårsak';
COMMENT ON COLUMN AVSLAGSARSAK.vilkar_kode IS 'Kode for vilkårstype som avslagsårsak kan brukes';
COMMENT ON COLUMN AVSLAGSARSAK.BESKRIVELSE IS 'Utdypende beskrivelse av avslagsårsak';

INSERT INTO AVSLAGSARSAK(avslag_kode, vilkar_kode, navn) VALUES ('1008', 'FP_VK_5', 'Søker er ikke barnets far');
INSERT INTO AVSLAGSARSAK(avslag_kode, vilkar_kode, navn) VALUES ('1009', 'FP_VK_5', 'Mor ikke død');
INSERT INTO AVSLAGSARSAK(avslag_kode, vilkar_kode, navn) VALUES ('1010', 'FP_VK_5', 'Mor ikke død ved fødsel/omsorg');
INSERT INTO AVSLAGSARSAK(avslag_kode, vilkar_kode, navn) VALUES ('1011', 'FP_VK_5', 'Engangsstønad er allerede utbetalt til mor');


-- Tabell: VILKAR
ALTER TABLE VILKAR ADD (avslag_kode VARCHAR2(20 CHAR));
ALTER TABLE VILKAR ADD CONSTRAINT FK_VILKAR_5 FOREIGN KEY (avslag_kode, vilkar_type) REFERENCES AVSLAGSARSAK;
