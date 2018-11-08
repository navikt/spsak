-- Slett det som er opprettet tidligere.
DROP TABLE YTELSE CASCADE CONSTRAINTS PURGE;
DROP TABLE YTELSE_GRUNNLAG CASCADE CONSTRAINTS PURGE;
DROP TABLE YTELSE_STOERRELSE CASCADE CONSTRAINTS PURGE;
DROP SEQUENCE SEQ_YTELSE;
DROP SEQUENCE SEQ_YTELSE_GRUNNLAG;
DROP SEQUENCE SEQ_YTELSE_STOERRELSE;


-- Mellomliggende AKTOER_YTELSE
CREATE TABLE AKTOER_YTELSE (
  id                        NUMBER(19)                        NOT NULL,
  inntekt_arbeid_ytelser_id NUMBER(19)                        NOT NULL,
  aktoer_id                 NUMBER(19)                        NOT NULL,
  versjon                   NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_AKTOER_YTELSE PRIMARY KEY (id),
  CONSTRAINT FK_AKTOER_YTELSE_1 FOREIGN KEY (inntekt_arbeid_ytelser_id) REFERENCES INNTEKT_ARBEID_YTELSER(id)
);

CREATE SEQUENCE SEQ_AKTOER_YTELSE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
-- SLUTT AKTOER_YTELSE


--- Oppdaterte YTELSE
CREATE TABLE YTELSE (
  id                       NUMBER(19)                        NOT NULL,
  aktoer_ytelse_id         NUMBER(19)                        NOT NULL,
  ytelse_type              VARCHAR2(100)                     NOT NULL,
  fom                      DATE                              NOT NULL,
  tom                      DATE                              NOT NULL,
  status                   VARCHAR2(100 CHAR)                NOT NULL,
  saksnummer               VARCHAR2(100)                     NOT NULL,
  kilde                    VARCHAR2(100)                     NOT NULL,
  kl_ytelse_type           VARCHAR2(100) AS ('RELATERT_YTELSE_TYPE'),
  kl_status                VARCHAR2(100 CHAR) AS ('RELATERT_YTELSE_TILSTAND'),
  kl_kilde                 VARCHAR2(100) AS ('FAGSYSTEM'),
  versjon                  NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av             VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid            TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                VARCHAR2(20 CHAR),
  endret_tid               TIMESTAMP(3),
  CONSTRAINT PK_YTELSE PRIMARY KEY (id),
  CONSTRAINT FK_YTELSE_1 foreign key (ytelse_type, kl_ytelse_type) references KODELISTE(kode, kodeverk),
  constraint FK_YTELSE_2 foreign key (status, kl_status) references KODELISTE(kode, kodeverk),
  constraint FK_YTELSE_3 foreign key (kilde, kl_kilde) references KODELISTE(kode, kodeverk),
  constraint FK_YTELSE_4 foreign key (aktoer_ytelse_id) references AKTOER_YTELSE(id)
);

CREATE SEQUENCE SEQ_YTELSE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE YTELSE  IS 'En tabell med informasjon om ytelser fra Arena og Infotrygd';
COMMENT ON COLUMN YTELSE.id IS 'PK';
COMMENT ON COLUMN YTELSE.aktoer_ytelse_id IS 'FK: AKTOER_YTELSE';
COMMENT ON COLUMN YTELSE.ytelse_type IS 'Type ytelse for eksempel sykepenger, foreldrepenger.. (dagpenger?) etc';
COMMENT ON COLUMN YTELSE.fom IS 'Startdato for ytelsten. Er tilsvarende Identdato fra Infotrygd.';
COMMENT ON COLUMN YTELSE.tom IS 'Sluttdato er en utledet dato enten fra opphørFOM eller fra identdaot pluss periode';
COMMENT ON COLUMN YTELSE.status IS 'Er om ytelsen er ÅPEN, LØPENDE eller AVSLUTTET';
COMMENT ON COLUMN YTELSE.saksnummer IS 'SakId fra Infotrygd og Arena';
COMMENT ON COLUMN YTELSE.kilde IS 'Hvilket system informasjonen kommer fra';
-- SLUTT AKTOER_YTELSE

--- YTELSE_GRUNNLAG
CREATE TABLE YTELSE_GRUNNLAG (
  id                       NUMBER(19)                        NOT NULL,
  ytelse_id                NUMBER(19)                        NOT NULL,
  arbeid_type              VARCHAR2(100)                     NOT NULL,
  opprinnelig_identdato    DATE                              ,
  dekningsgrad_prosent     DECIMAL(5, 2)                     ,
  gradering_prosent        DECIMAL(5, 2)                     ,
  inntektsgrunnlag_prosent DECIMAL(5, 2)                     ,
  kl_arbeid_type           VARCHAR2(100) AS ('ARBEID_TYPE'),
  versjon                  NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av             VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid            TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                VARCHAR2(20 CHAR),
  endret_tid               TIMESTAMP(3),
  CONSTRAINT PK_YTELSE_GRUNNLAG PRIMARY KEY (id),
  constraint FK_YTELSE_GRUNNLAG_1 foreign key (arbeid_type, kl_arbeid_type) references KODELISTE(kode, kodeverk),
  constraint FK_YTELSE_GRUNNLAG_2 foreign key (ytelse_id) references YTELSE(id)
);

CREATE SEQUENCE SEQ_YTELSE_GRUNNLAG MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE YTELSE_GRUNNLAG IS 'En tabell med informasjon om ytelsesgrunnlag fra Arena og Infotrygd';
COMMENT ON COLUMN YTELSE_GRUNNLAG.id IS 'PK';
COMMENT ON COLUMN YTELSE_GRUNNLAG.ytelse_id IS 'FK: YTELSE';
COMMENT ON COLUMN YTELSE_GRUNNLAG.arbeid_type IS 'Hva slags type arbeid det er. Tilsvarer Arbeidskategori/behandlingstema hos Infotrygd og Arena';
COMMENT ON COLUMN YTELSE_GRUNNLAG.opprinnelig_identdato IS 'Identdato (samme som stardato. kan hende denne er overflødig';
COMMENT ON COLUMN YTELSE_GRUNNLAG.dekningsgrad_prosent IS 'Dekningsgrad hentet fra infotrygd';
COMMENT ON COLUMN YTELSE_GRUNNLAG.gradering_prosent IS 'Gradering hentet fra infotrygd';
COMMENT ON COLUMN YTELSE_GRUNNLAG.inntektsgrunnlag_prosent IS 'Inntektsgrunnlag hentet fra infotrygd';
-- SLUTT YTELSE_GRUNNLAG

-- NY YTELSE_STOERRELSE
CREATE TABLE YTELSE_STOERRELSE (
  id                       NUMBER(19)                        NOT NULL,
  ytelse_grunnlag_id       NUMBER(19)                        NOT NULL,
  virksomhet_id            NUMBER(19)                        NOT NULL,
  beloep                   DECIMAL(19, 2)                    NOT NULL,
  hyppighet                VARCHAR2(100)                     NOT NULL,
  kl_hyppighet             VARCHAR2(100) AS ('INNTEKTS_PERIODE_TYPE'),
  versjon                  NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av             VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid            TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                VARCHAR2(20 CHAR),
  endret_tid               TIMESTAMP(3),
  CONSTRAINT PK_YTELSE_STOERRELSE PRIMARY KEY (id),
  constraint FK_YTELSE_STOERRELSE_1 foreign key (hyppighet, kl_hyppighet) references KODELISTE(kode, kodeverk),
  constraint FK_YTELSE_STOERRELSE_2 foreign key (ytelse_grunnlag_id) references YTELSE_GRUNNLAG(id),
  constraint FK_YTELSE_STOERRELSE_3 foreign key (virksomhet_id) references VIRKSOMHET(id)
);

CREATE SEQUENCE SEQ_YTELSE_STOERRELSE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE YTELSE_STOERRELSE IS 'En tabell med informasjon om beløpene som kommer fra ytelsesgrunnlag fra Arena og Infotrygd';
COMMENT ON COLUMN YTELSE_STOERRELSE.id IS 'PK';
COMMENT ON COLUMN YTELSE_STOERRELSE.ytelse_grunnlag_id IS 'FK: YTELSE_GRUNNLAG';
COMMENT ON COLUMN YTELSE_STOERRELSE.virksomhet_id IS 'FK: VIRKSOMHET';
COMMENT ON COLUMN YTELSE_STOERRELSE.beloep IS 'Beløpet som er for den gitte perioden i ytelsesgrunnlag';
COMMENT ON COLUMN YTELSE_STOERRELSE.hyppighet IS 'Hyppigheten for beløpet';
-- SLUTT YTELSE_STORRELSE

---- NY ANVIST
CREATE TABLE YTELSE_ANVIST (
  id                       NUMBER(19)                        NOT NULL,
  ytelse_id                NUMBER(19)                        NOT NULL,
  beloep                   DECIMAL(19, 2)                     ,
  fom                      DATE                              NOT NULL,
  tom                      DATE                              NOT NULL,
  utbetalingsgrad_prosent  DECIMAL(5, 2)                      ,
  versjon                  NUMBER(19) DEFAULT 0              NOT NULL,
  opprettet_av             VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid            TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                VARCHAR2(20 CHAR),
  endret_tid               TIMESTAMP(3),
  CONSTRAINT PK_YTELSE_ANVIST PRIMARY KEY (id),
  constraint FK_YTELSE_ANVIST_1 foreign key (ytelse_id) references YTELSE(id)
);

CREATE SEQUENCE SEQ_YTELSE_ANVIST MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE YTELSE_ANVIST IS 'En tabell med informasjon om ytelsesperioder';
COMMENT ON COLUMN YTELSE_ANVIST.id IS 'PK';
COMMENT ON COLUMN YTELSE_ANVIST.ytelse_id IS 'FK: YTELSE';
COMMENT ON COLUMN YTELSE_ANVIST.beloep IS 'Beløp ifm utbetaling.';
COMMENT ON COLUMN YTELSE_ANVIST.fom IS 'Anvist periode første dag.';
COMMENT ON COLUMN YTELSE_ANVIST.tom IS 'Anvist periode siste dag.';
COMMENT ON COLUMN YTELSE_ANVIST.utbetalingsgrad_prosent IS 'Utbetalingsprosent fra kildesystem.';
-- SLUTT YTELSE_GANVIST
