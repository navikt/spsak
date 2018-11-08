-- START -> Ny tabell
CREATE TABLE YTELSE (
  id                          NUMBER(19)                        NOT NULL,
  inntekt_arbeid_ytelser_id   NUMBER(19)                        NOT NULL,
  ytelse_type                 VARCHAR2(100)                     NOT NULL,
  fom                         DATE                              NOT NULL,
  tom                         DATE                              NOT NULL,
  status                      NUMBER(19)                        NOT NULL,
  saksnummer                  VARCHAR2(100)                     NOT NULL,
  kilde                       VARCHAR2(100)                     NOT NULL,
  kl_ytelse_type              VARCHAR2(100) AS ('RELATERT_YTELSE_TYPE'),
  kl_kilde                    VARCHAR2(100) AS ('FAGSYSTEM'),
  opprettet_av                VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid               TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                   VARCHAR2(20 CHAR),
  endret_tid                  TIMESTAMP(3),
  CONSTRAINT PK_YTELSE PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_YTELSE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

alter table YTELSE add constraint FK_YTELSE_1 foreign key (ytelse_type, kl_ytelse_type) references KODELISTE(kode, kodeverk);
alter table YTELSE add constraint FK_YTELSE_2 foreign key (kilde, kl_kilde) references KODELISTE(kode, kodeverk);
alter table YTELSE add constraint FK_YTELSE_3 foreign key (inntekt_arbeid_ytelser_id) references INNTEKT_ARBEID_YTELSER(id);

COMMENT ON TABLE YTELSE  IS 'En tabell med informasjon om ytelser fra Arena og Infotrygd';
COMMENT ON COLUMN YTELSE.id IS 'PK';
COMMENT ON COLUMN YTELSE.inntekt_arbeid_ytelser_id IS 'FK: INNTEKT_ARBEID_YTELSER';
COMMENT ON COLUMN YTELSE.ytelse_type IS 'Type ytelse for eksempel sykepenger, foreldrepenger.. (dagpenger?) etc';
COMMENT ON COLUMN YTELSE.fom IS 'Startdato for ytelsten. Er tilsvarende Identdato fra Infotrygd.';
COMMENT ON COLUMN YTELSE.tom IS 'Sluttdato er en utledet dato enten fra opphørFOM eller fra identdaot pluss periode';
COMMENT ON COLUMN YTELSE.status IS 'Er om ytelsen er ÅPEN, LØPENDE eller AVSLUTTET';
COMMENT ON COLUMN YTELSE.saksnummer IS 'SakId fra Infotrygd og Arena';
COMMENT ON COLUMN YTELSE.kilde IS 'Hvilket system informasjonen kommer fra';

CREATE TABLE YTELSE_GRUNNLAG (
  id                          NUMBER(19)                        NOT NULL,
  ytelse_id                   NUMBER(19)                        NOT NULL,
  opprinnelig_identdato       DATE                              NOT NULL,
  arbeid_type                 VARCHAR2(100)                     NOT NULL,
  utbetalingsgrad_prosent     DECIMAL(5, 2)                     NOT NULL,
  dekningsgrad_prosent        DECIMAL(5, 2)                     NOT NULL,
  gradering_prosent           DECIMAL(5, 2)                     NOT NULL,
  inntektsgrunnlag_prosent    DECIMAL(5, 2)                     NOT NULL,
  kl_arbeid_type              VARCHAR2(100) AS ('ARBEID_TYPE'),
  opprettet_av                VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid               TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                   VARCHAR2(20 CHAR),
  endret_tid                  TIMESTAMP(3),
  CONSTRAINT PK_YTELSE_GRUNNLAG PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_YTELSE_GRUNNLAG MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

alter table YTELSE_GRUNNLAG add constraint FK_YTELSE_GRUNNLAG_1 foreign key (arbeid_type, kl_arbeid_type) references KODELISTE(kode, kodeverk);
alter table YTELSE_GRUNNLAG add constraint FK_YTELSE_GRUNNLAG_2 foreign key (ytelse_id) references YTELSE(id);

COMMENT ON TABLE YTELSE_GRUNNLAG IS 'En tabell med informasjon om ytelsesgrunnlag fra Arena og Infotrygd';
COMMENT ON COLUMN YTELSE_GRUNNLAG.id IS 'PK';
COMMENT ON COLUMN YTELSE_GRUNNLAG.ytelse_id IS 'FK: YTELSE';
COMMENT ON COLUMN YTELSE_GRUNNLAG.opprinnelig_identdato IS 'Identdato (samme som stardato. kan hende denne er overflødig';
COMMENT ON COLUMN YTELSE_GRUNNLAG.arbeid_type IS 'Hva slags type arbeid det er. Tilsvarer Arbeidskategori/behandlingstema hos Infotrygd og Arena';
COMMENT ON COLUMN YTELSE_GRUNNLAG.utbetalingsgrad_prosent IS 'Utbetalingsprosent. Her kan det være problemer med Arena sin utbetalingsprosent for den er 200% for 100%';
COMMENT ON COLUMN YTELSE_GRUNNLAG.dekningsgrad_prosent IS 'Dekningsgrad hentet fra infotrygd';
COMMENT ON COLUMN YTELSE_GRUNNLAG.gradering_prosent IS 'Gradering hentet fra infotrygd';
COMMENT ON COLUMN YTELSE_GRUNNLAG.inntektsgrunnlag_prosent IS 'Inntektsgrunnlag hentet fra infotrygd';


CREATE TABLE YTELSE_STOERRELSE (
  id                          NUMBER(19)                        NOT NULL,
  ytelse_grunnlag_id          NUMBER(19)                        NOT NULL,
  virksomhet_id               NUMBER(19)                        NOT NULL,
  beloep                      DECIMAL(19, 2)                    NOT NULL,
  fom                         DATE                              NOT NULL,
  tom                         DATE                              NOT NULL,
  hyppighet                   VARCHAR2(100)                     NOT NULL,
  kl_hyppighet                VARCHAR2(100) AS ('INNTEKTS_PERIODE_TYPE'),
  opprettet_av                VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid               TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                   VARCHAR2(20 CHAR),
  endret_tid                  TIMESTAMP(3),
  CONSTRAINT PK_YTELSE_STOERRELSE PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_YTELSE_STOERRELSE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

alter table YTELSE_STOERRELSE add constraint FK_YTELSE_STOERRELSE_1 foreign key (hyppighet, kl_hyppighet) references KODELISTE(kode, kodeverk);
alter table YTELSE_STOERRELSE add constraint FK_YTELSE_STOERRELSE_2 foreign key (ytelse_grunnlag_id) references YTELSE_GRUNNLAG(id);
alter table YTELSE_STOERRELSE add constraint FK_YTELSE_STOERRELSE_3 foreign key (virksomhet_id) references VIRKSOMHET(id);

COMMENT ON TABLE YTELSE_STOERRELSE IS 'En tabell med informasjon om beløpene som kommer fra ytelsesgrunnlag fra Arena og Infotrygd';
COMMENT ON COLUMN YTELSE_STOERRELSE.id IS 'PK';
COMMENT ON COLUMN YTELSE_STOERRELSE.ytelse_grunnlag_id IS 'FK: YTELSE_GRUNNLAG';
COMMENT ON COLUMN YTELSE_STOERRELSE.virksomhet_id IS 'FK: VIRKSOMHET';
COMMENT ON COLUMN YTELSE_STOERRELSE.beloep IS 'Beløpet som er for den gitte perioden i ytelsesgrunnlag';
COMMENT ON COLUMN YTELSE_STOERRELSE.fom IS 'Fom for perioden beløpet gjelder for';
COMMENT ON COLUMN YTELSE_STOERRELSE.tom IS 'Tom for perioden beløpet gjelder for';
COMMENT ON COLUMN YTELSE_STOERRELSE.hyppighet IS 'Hyppigheten for beløpet';


INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('INNTEKT_PERIODE_TYPE', 'Inntektsperiodetyper', 'Perioder som inntekter kan beregnes på', 'VL', 'N', 'N');

INSERT INTO KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom, ekstra_data)
VALUES (seq_kodeliste.nextval, 'DAGLG', 'D', 'Daglig','Inntekt gitt per dag', 'INNTEKT_PERIODE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'), 'P1D');

INSERT INTO KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom, ekstra_data)
VALUES (seq_kodeliste.nextval, 'UKNLG', 'U', 'Ukentlig','Inntekt gitt per uke', 'INNTEKT_PERIODE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'), 'P1W');

INSERT INTO KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom, ekstra_data)
VALUES (seq_kodeliste.nextval, '14DLG', 'D', 'Fjorten-daglig','Inntekt gitt per fjorten dager', 'INNTEKT_PERIODE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'), 'P2W');

INSERT INTO KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom, ekstra_data)
VALUES (seq_kodeliste.nextval, 'MNDLG', 'D', 'Månedlig','Inntekt gitt per måned', 'INNTEKT_PERIODE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'), 'P1M');

INSERT INTO KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom, ekstra_data)
VALUES (seq_kodeliste.nextval, 'AARLG', 'D', 'Årlig','Inntekt gitt per år', 'INNTEKT_PERIODE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'), 'P1Y');

INSERT INTO KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNFS', 'X', 'Fastsatt etter 25 prosent avvik','Inntekt som er fastsatt etter 25 prosent avvik', 'INNTEKT_PERIODE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'PREMGR', 'Y', 'Premiegrunnlag',' Premiegrunnlag oppdragstaker (gjelder de to første ukene) ', 'INNTEKT_PERIODE_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
