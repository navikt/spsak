/* NYE/ENDREDE tabeller - START */
-- Tabell: PERIODE_TYPE
CREATE TABLE PERIODE_TYPE (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_PERIODE_TYPE PRIMARY KEY (kode)
);

COMMENT ON TABLE PERIODE_TYPE  IS 'Kodeverk for type oppholdsperiode';
COMMENT ON COLUMN PERIODE_TYPE.KODE IS 'Kodeverk Primary Key';
COMMENT ON COLUMN PERIODE_TYPE.BESKRIVELSE IS 'Utdypende beskrivelse av koden';

INSERT INTO PERIODE_TYPE(kode, navn) VALUES ('AR', 'År');
INSERT INTO PERIODE_TYPE(kode, navn) VALUES ('MANED', 'Måned');
INSERT INTO PERIODE_TYPE(kode, navn) VALUES ('DAG', 'Dag');


-- Tabell: TILKNYTNING_HJEMLAND
RENAME TILKNYTNING_NORGE TO TILKNYTNING_HJEMLAND;
ALTER TABLE TILKNYTNING_HJEMLAND RENAME CONSTRAINT PK_TILKNYTNING_NORGE TO PK_TILKNYTNING_HJEMLAND;
ALTER TABLE TILKNYTNING_HJEMLAND ADD oppholdSistePeriode VARCHAR2(1 CHAR);
ALTER TABLE TILKNYTNING_HJEMLAND ADD oppholdNestePeriode VARCHAR2(1 CHAR);
ALTER TABLE TILKNYTNING_HJEMLAND ADD periodeType VARCHAR2(20 CHAR);
ALTER TABLE TILKNYTNING_HJEMLAND ADD CONSTRAINT FK_PERIODE_TYPE FOREIGN KEY (periodeType) REFERENCES PERIODE_TYPE;

RENAME SEQ_TILKNYTNING_NORGE TO SEQ_TILKNYTNING_HJEMLAND;
ALTER TABLE SOEKNAD RENAME COLUMN tilknytning_norge_id TO tilknytning_hjemland_id;
ALTER TABLE SOEKNAD RENAME CONSTRAINT FK_SOEKNAD_TILKNYTNING_NORGE TO FK_SOEKNAD_TILKNYTNING_HJEML;

ALTER TABLE UTLANDSOPPHOLD RENAME COLUMN opphold_norge TO opphold_hjemland;

-- Tabell: INNTEKT
CREATE TABLE INNTEKT (
  id                             NUMBER(19) NOT NULL,
  behandling_grunnlag_id         NUMBER(19) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  utbetaler                      VARCHAR2(50 CHAR),
  fom                            DATE,
  tom                            DATE,
  ytelse                         VARCHAR2(1 CHAR) NOT NULL,
  belop                          NUMBER(19) NOT NULL,
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_INNTEKT PRIMARY KEY (id),
  CONSTRAINT FK_INNTEKT_1 FOREIGN KEY (behandling_grunnlag_id) REFERENCES BEHANDLING_GRUNNLAG
);

CREATE INDEX IDX_INNTEKT_1 ON INNTEKT(behandling_grunnlag_id);

CREATE SEQUENCE SEQ_INNTEKT MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE INNTEKT IS 'Inntekter og ytelser for søker og oppgitt annen forelder';
COMMENT ON COLUMN INNTEKT.navn IS 'Navn på mottaker';
COMMENT ON COLUMN INNTEKT.utbetaler IS 'Navn på utbetaler(arbeidsgiver) eller ytelse(f.eks. sykepenger).';
COMMENT ON COLUMN INNTEKT.fom IS 'Fra og med dato';
COMMENT ON COLUMN INNTEKT.tom IS 'Til og med dato';
COMMENT ON COLUMN INNTEKT.ytelse IS 'True dersom radern gjelder ytelse, false dersom inntekt.';
COMMENT ON COLUMN INNTEKT.belop IS 'Beløp som er utbetalt';


-- Tabell: MEDLEMSKAP_TYPE
CREATE TABLE MEDLEMSKAP_TYPE (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_MEDLEMSKAP_TYPE PRIMARY KEY (kode)
);

COMMENT ON TABLE MEDLEMSKAP_TYPE  IS 'Kodeverk for type medlemsskap i folketrygden';
COMMENT ON COLUMN MEDLEMSKAP_TYPE.KODE IS 'Kodeverk Primary Key';
COMMENT ON COLUMN MEDLEMSKAP_TYPE.BESKRIVELSE IS 'Utdypende beskrivelse av koden';

INSERT INTO MEDLEMSKAP_TYPE(kode, navn) VALUES ('ENDELIG', 'Endelig');
INSERT INTO MEDLEMSKAP_TYPE(kode, navn) VALUES ('FORELOPIG', 'Foreløpig');
INSERT INTO MEDLEMSKAP_TYPE(kode, navn) VALUES ('AVKLARES', 'Under avklaring');


-- Tabell: MEDLEMSKAP_DEKNING_TYPE
CREATE TABLE MEDLEMSKAP_DEKNING_TYPE (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_MEDLEMSKAP_DEKNING_TYPE PRIMARY KEY (kode)
);

COMMENT ON TABLE MEDLEMSKAP_DEKNING_TYPE  IS 'Kodeverk for medlemsskapsdekning i folketrygden';
COMMENT ON COLUMN MEDLEMSKAP_DEKNING_TYPE.KODE IS 'Kodeverk Primary Key';
COMMENT ON COLUMN MEDLEMSKAP_DEKNING_TYPE.BESKRIVELSE IS 'Utdypende beskrivelse av koden';

INSERT INTO MEDLEMSKAP_DEKNING_TYPE(kode, navn) VALUES ('A', 'A');
INSERT INTO MEDLEMSKAP_DEKNING_TYPE(kode, navn) VALUES ('B', 'B');
INSERT INTO MEDLEMSKAP_DEKNING_TYPE(kode, navn) VALUES ('C', 'C');


-- Tabell: MEDLEMSKAP_KILDE_TYPE
CREATE TABLE MEDLEMSKAP_KILDE_TYPE (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_MEDLEMSKAP_KILDE_TYPE PRIMARY KEY (kode)
);

COMMENT ON TABLE MEDLEMSKAP_KILDE_TYPE  IS 'Kodeverk for kilde til informasjon om medlemsskap i folketrygden';
COMMENT ON COLUMN MEDLEMSKAP_KILDE_TYPE.KODE IS 'Kodeverk Primary Key';
COMMENT ON COLUMN MEDLEMSKAP_KILDE_TYPE.BESKRIVELSE IS 'Utdypende beskrivelse av koden';

INSERT INTO MEDLEMSKAP_KILDE_TYPE(kode, navn) VALUES ('GOSYS', 'Gosys');
INSERT INTO MEDLEMSKAP_KILDE_TYPE(kode, navn) VALUES ('LANEKASSEN', 'Lånekassen');
INSERT INTO MEDLEMSKAP_KILDE_TYPE(kode, navn) VALUES ('AVSYS', 'AvSys');


-- Tabell: MEDLEMSKAP_PERIODER
CREATE TABLE MEDLEMSKAP_PERIODER (
  id                             NUMBER(19) NOT NULL,
  behandling_grunnlag_id         NUMBER(19) NOT NULL,
  fom                            DATE,
  tom                            DATE,
  medlemskapType                 VARCHAR2(20 CHAR),
  dekningType                    VARCHAR2(20 CHAR),
  kildeType                      VARCHAR2(20 CHAR),
  beslutningsdato                DATE,
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_MEDLEMSKAP_PERIODER PRIMARY KEY (id),
  CONSTRAINT FK_MEDLEMSKAP_PERIODER_1 FOREIGN KEY (behandling_grunnlag_id) REFERENCES BEHANDLING_GRUNNLAG,
  CONSTRAINT FK_MEDLEMSKAP_PERIODER_2 FOREIGN KEY (medlemskapType) REFERENCES MEDLEMSKAP_TYPE,
  CONSTRAINT FK_MEDLEMSKAP_PERIODER_3 FOREIGN KEY (dekningType) REFERENCES MEDLEMSKAP_DEKNING_TYPE,
  CONSTRAINT FK_MEDLEMSKAP_PERIODER_4 FOREIGN KEY (kildeType) REFERENCES MEDLEMSKAP_KILDE_TYPE
);

CREATE INDEX IDX_MEDLEMSKAP_PERIODER_1 ON MEDLEMSKAP_PERIODER(behandling_grunnlag_id);
CREATE INDEX IDX_MEDLEMSKAP_PERIODER_2 ON MEDLEMSKAP_PERIODER(medlemskapType);
CREATE INDEX IDX_MEDLEMSKAP_PERIODER_3 ON MEDLEMSKAP_PERIODER(dekningType);
CREATE INDEX IDX_MEDLEMSKAP_PERIODER_4 ON MEDLEMSKAP_PERIODER(kildeType);

CREATE SEQUENCE SEQ_MEDLEMSKAP_PERIODER MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE MEDLEMSKAP_PERIODER IS 'Perioder med medlemsskap i folketrygden';
COMMENT ON COLUMN MEDLEMSKAP_PERIODER.fom IS 'Fra og med dato';
COMMENT ON COLUMN MEDLEMSKAP_PERIODER.tom IS 'Til og med dato';
COMMENT ON COLUMN MEDLEMSKAP_PERIODER.beslutningsdato IS 'Beslutningsdato';


-- Tabell: PERSONSTATUS_TYPE
CREATE TABLE PERSONSTATUS_TYPE (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_PERSONSTATUS_TYPE PRIMARY KEY (kode)
);

COMMENT ON TABLE PERSONSTATUS_TYPE  IS 'Kodeverk for personstatus';
COMMENT ON COLUMN PERSONSTATUS_TYPE.KODE IS 'Kodeverk Primary Key';
COMMENT ON COLUMN PERSONSTATUS_TYPE.BESKRIVELSE IS 'Utdypende beskrivelse av koden';

INSERT INTO PERSONSTATUS_TYPE(kode, navn) VALUES ('ABNR', 'Aktivt BOSTNR');
INSERT INTO PERSONSTATUS_TYPE(kode, navn) VALUES ('ADNR', 'Aktivt');
INSERT INTO PERSONSTATUS_TYPE(kode, navn) VALUES ('BOSA', 'Bosatt');
INSERT INTO PERSONSTATUS_TYPE(kode, navn) VALUES ('DØD', 'Død');
INSERT INTO PERSONSTATUS_TYPE(kode, navn) VALUES ('FOSV', 'Forsvunnet/savnet');
INSERT INTO PERSONSTATUS_TYPE(kode, navn) VALUES ('FØDR', 'Fødselregistrert');
INSERT INTO PERSONSTATUS_TYPE(kode, navn) VALUES ('UFUL', 'Ufullstendig fødselsnr');
INSERT INTO PERSONSTATUS_TYPE(kode, navn) VALUES ('UREG', 'Uregistrert person');
INSERT INTO PERSONSTATUS_TYPE(kode, navn) VALUES ('UTAN', 'Utgått person annullert tilgang Fnr');
INSERT INTO PERSONSTATUS_TYPE(kode, navn) VALUES ('UTPE', 'Utgått person');
INSERT INTO PERSONSTATUS_TYPE(kode, navn) VALUES ('UTVA', 'Utvandret');


-- Tabell: FORELDRE_TYPE
CREATE TABLE FORELDRE_TYPE (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_FORELDRE_TYPE PRIMARY KEY (kode)
);

COMMENT ON TABLE FORELDRE_TYPE  IS 'Kodeverk for foreldreansvarstype';
COMMENT ON COLUMN FORELDRE_TYPE.KODE IS 'Kodeverk Primary Key';
COMMENT ON COLUMN FORELDRE_TYPE.BESKRIVELSE IS 'Utdypende beskrivelse av koden';

INSERT INTO FORELDRE_TYPE(kode, navn) VALUES ('ANDRE', '3. part');
INSERT INTO FORELDRE_TYPE(kode, navn) VALUES ('MOR', 'Mor');
INSERT INTO FORELDRE_TYPE(kode, navn) VALUES ('FAR', 'Far');
INSERT INTO FORELDRE_TYPE(kode, navn) VALUES ('MEDMOR', 'Medmor');
INSERT INTO FORELDRE_TYPE(kode, navn) VALUES ('MEDFAR', 'Medfar');


-- Tabell: BEKREFTET_FORELDRE
ALTER TABLE BEKREFTET_FORELDRE ADD personstatusType VARCHAR2(20 CHAR);
ALTER TABLE BEKREFTET_FORELDRE ADD CONSTRAINT FK_BEKREFTET_FORELDRE_3 FOREIGN KEY (personstatusType) REFERENCES PERSONSTATUS_TYPE;
CREATE INDEX IDX_BEKREFTET_FORELDRE_2 ON BEKREFTET_FORELDRE(personstatusType);

ALTER TABLE BEKREFTET_FORELDRE ADD foreldreType VARCHAR2(20 CHAR);
ALTER TABLE BEKREFTET_FORELDRE ADD CONSTRAINT FK_BEKREFTET_FORELDRE_4 FOREIGN KEY (foreldreType) REFERENCES FORELDRE_TYPE;
CREATE INDEX IDX_BEKREFTET_FORELDRE_3 ON BEKREFTET_FORELDRE(foreldreType);

ALTER TABLE BEKREFTET_FORELDRE ADD utlandsadresse VARCHAR2(500 CHAR);
COMMENT ON COLUMN BEKREFTET_FORELDRE.utlandsadresse IS 'Utlandsadresse';


-- Tabell: REGION
CREATE TABLE REGION (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_REGION PRIMARY KEY (kode)
);

COMMENT ON TABLE REGION  IS 'Kodeverk for landregion';
COMMENT ON COLUMN REGION.KODE IS 'Kodeverk Primary Key';
COMMENT ON COLUMN REGION.BESKRIVELSE IS 'Utdypende beskrivelse av koden';

INSERT INTO REGION(kode, navn) VALUES ('NORDEN', 'Norden');
INSERT INTO REGION(kode, navn) VALUES ('EOS', 'EØS');
INSERT INTO REGION(kode, navn) VALUES ('ANNET', 'Annet');

-- Tabell: LAND_REGION
CREATE TABLE LAND_REGION (
  landkode                       VARCHAR2(20 CHAR) NOT NULL,
  region                         VARCHAR2(20 CHAR) NOT NULL,
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_LAND_REGION PRIMARY KEY (landkode, region),
  CONSTRAINT FK_LAND_REGION_1 FOREIGN KEY (region) REFERENCES REGION,
  CONSTRAINT FK_LAND_REGION_2 FOREIGN KEY (landkode) REFERENCES LANDKODER
);

COMMENT ON TABLE LAND_REGION IS 'Kodeverk for land og region';
COMMENT ON COLUMN LAND_REGION.landkode IS 'Landkode';
COMMENT ON COLUMN LAND_REGION.region IS 'Geografisk område';


-- Tabell: STATSBORGERSKAP
CREATE TABLE STATSBORGERSKAP (
  id                             NUMBER(19) NOT NULL,
  bekreftet_foreldre_id          NUMBER(19) NOT NULL,
  landkode                       VARCHAR2(20 CHAR) NOT NULL,
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_STATSBORGERSKAP PRIMARY KEY (id),
  CONSTRAINT FK_STATSBORGERSKAP_1 FOREIGN KEY (bekreftet_foreldre_id) REFERENCES BEKREFTET_FORELDRE,
  CONSTRAINT FK_STATSBORGERSKAP_2 FOREIGN KEY (landkode) REFERENCES LANDKODER
);

CREATE INDEX IDX_STATSBORGERSKAP_1 ON STATSBORGERSKAP(bekreftet_foreldre_id);
CREATE INDEX IDX_STATSBORGERSKAP_2 ON STATSBORGERSKAP(landkode);

CREATE SEQUENCE SEQ_STATSBORGERSKAP MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE STATSBORGERSKAP IS 'Statsborgerskap for foreldre';


-- Tabell: MEDLEMSKAP
CREATE TABLE MEDLEMSKAP (
  id                             NUMBER(19) NOT NULL,
  behandling_grunnlag_id         NUMBER(19) NOT NULL,
  regionForSoker                 VARCHAR2(20 CHAR),
  oppholdsrettVurdering          VARCHAR2(1 CHAR),
  lovligOppholdVurdering         VARCHAR2(1 CHAR),
  bosattVurdering                VARCHAR2(1 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_MEDLEMSKAP PRIMARY KEY (id),
  CONSTRAINT FK_MEDLEMSKAP_1 FOREIGN KEY (behandling_grunnlag_id) REFERENCES BEHANDLING_GRUNNLAG,
  CONSTRAINT FK_MEDLEMSKAP_2 FOREIGN KEY (regionForSoker) REFERENCES REGION
);

CREATE INDEX IDX_MEDLEMSKAP_1 ON MEDLEMSKAP(behandling_grunnlag_id);
CREATE INDEX IDX_MEDLEMSKAP_2 ON MEDLEMSKAP(regionForSoker);

CREATE SEQUENCE SEQ_MEDLEMSKAP MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE MEDLEMSKAP IS 'Status for medlemskap i folketrygden';
COMMENT ON COLUMN MEDLEMSKAP.regionForSoker IS 'Region for søker';
COMMENT ON COLUMN MEDLEMSKAP.oppholdsrettVurdering IS 'True hvis søker har oppholdsrett';
COMMENT ON COLUMN MEDLEMSKAP.lovligOppholdVurdering IS 'True hvis søker har lovlig opphold';
COMMENT ON COLUMN MEDLEMSKAP.bosattVurdering IS 'True hvis søker er bosatt';

/* NYE/ENDREDE tabeller - SLUTT */


/* ENDRINGER I PROSESSMODELL - START */

-- Endre navn på vurderingssteg
ALTER TABLE VURDERINGSPUNKT_DEF DISABLE CONSTRAINT FK_VURDERINGSPUNKT_DEF_1;
ALTER TABLE AKSJONSPUNKT_DEF DISABLE CONSTRAINT FK_AKSJONSPUNKT_DEF_2;
ALTER TABLE BEHANDLING_TYPE_STEG_SEKV DISABLE CONSTRAINT FK_BEHANDLING_TYPE_STEG_SEK_01;

UPDATE BEHANDLING_STEG_TYPE SET KODE = 'VURDERBV' WHERE KODE = 'VUBIN';
UPDATE BEHANDLING_STEG_TYPE SET KODE = 'VURDERSFV' WHERE KODE = 'VUFIN';

UPDATE VURDERINGSPUNKT_DEF SET BEHANDLING_STEG = 'VURDERBV', KODE = 'VURDERBV.INN'  WHERE BEHANDLING_STEG = 'VUBIN' AND KODE = 'VUBIN.INN';
UPDATE VURDERINGSPUNKT_DEF SET BEHANDLING_STEG = 'VURDERBV', KODE = 'VURDERBV.UT'  WHERE BEHANDLING_STEG = 'VUBIN' AND KODE = 'VUBIN.UT';
UPDATE VURDERINGSPUNKT_DEF SET BEHANDLING_STEG = 'VURDERSFV', KODE = 'VURDERSFV.INN'  WHERE BEHANDLING_STEG = 'VUFIN' AND KODE = 'VUFIN.INN';
UPDATE VURDERINGSPUNKT_DEF SET BEHANDLING_STEG = 'VURDERSFV', KODE = 'VURDERSFV.UT'  WHERE BEHANDLING_STEG = 'VUFIN' AND KODE = 'VUFIN.UT';

UPDATE AKSJONSPUNKT_DEF SET VURDERINGSPUNKT = 'VURDERBV.INN' where VURDERINGSPUNKT = 'VUBIN.INN';
UPDATE AKSJONSPUNKT_DEF SET VURDERINGSPUNKT = 'VURDERBV.UT' where VURDERINGSPUNKT = 'VUBIN.UT';
UPDATE AKSJONSPUNKT_DEF SET VURDERINGSPUNKT = 'VURDERSFV.INN' where VURDERINGSPUNKT = 'VUFIN.INN';
UPDATE AKSJONSPUNKT_DEF SET VURDERINGSPUNKT = 'VURDERSFV.UT' where VURDERINGSPUNKT = 'VUFIN.UT';

UPDATE BEHANDLING_TYPE_STEG_SEKV SET BEHANDLING_STEG_TYPE = 'VURDERBV' WHERE BEHANDLING_STEG_TYPE = 'VUBIN';
UPDATE BEHANDLING_TYPE_STEG_SEKV SET BEHANDLING_STEG_TYPE = 'VURDERSFV' WHERE BEHANDLING_STEG_TYPE = 'VUFIN';

ALTER TABLE VURDERINGSPUNKT_DEF ENABLE CONSTRAINT FK_VURDERINGSPUNKT_DEF_1;
ALTER TABLE AKSJONSPUNKT_DEF ENABLE CONSTRAINT FK_AKSJONSPUNKT_DEF_2;
ALTER TABLE BEHANDLING_TYPE_STEG_SEKV ENABLE CONSTRAINT FK_BEHANDLING_TYPE_STEG_SEK_01;

-- Introdusere nytt vurderingssteg for medlemskapvilkår
INSERT INTO BEHANDLING_STEG_TYPE(kode, navn, behandling_status_def, beskrivelse)
VALUES ('VURDERMV', 'Vurder medlemskapvilkår', 'UTRED', 'Vurdering av medlemskapsvilkåret');

INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
VALUES ('VURDERMV.INN', 'Vurder medlemskapvilkår - Inngang', 'VURDERMV', 'INN');
INSERT INTO VURDERINGSPUNKT_DEF (kode, navn, behandling_steg, vurderingspunkt_type)
VALUES ('VURDERMV.UT', 'Vurder medlemskapvilkår - Utgang', 'VURDERMV', 'UT');

UPDATE BEHANDLING_TYPE_STEG_SEKV SET sekvens_nr = sekvens_nr + 1 WHERE sekvens_nr >= 4;
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (id, behandling_type, behandling_steg_type, sekvens_nr)
VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.nextval, 'BT-002', 'VURDERMV', 4);

-- Utsett aksjonspunkter for kontroll av fakta til senest mulige tidspunkt, dvs. når dens tilhørende vilkår skal vurderes
-- Steg "Kontroller fakta"/UT: Alle aksjonspunkter nødvendige for å kunne bestemme vilkår
UPDATE AKSJONSPUNKT_DEF SET
  VURDERINGSPUNKT = 'KOFAK.UT'
WHERE NAVN IN (
  'Avklar fakta for omsorgs/foreldreansvarsvilkåret'
);
-- Steg "Vurder betinget vilkår"/INN: Aksjonspunkter for faktakontroll knyttet til BETINGET vilkår
UPDATE AKSJONSPUNKT_DEF SET
  VURDERINGSPUNKT = 'VURDERBV.INN'
WHERE NAVN IN (
  'Avklar terminbekreftelse',
  'Avklar fødsel',
  'Avklar antall barn',
  'Avklar adopsjonsdokumentasjon',
  'Avklar om adopsjon gjelder ektefelles barn',
  'Avklar om søker er mann adopterer alene',
  'Avklar tilleggsopplysninger'
);
-- Steg "Vurder betinget vilkår"/UT: Aksjonspunkter for manuell vurdering knyttet til BETINGET vilkår
UPDATE AKSJONSPUNKT_DEF SET
  VURDERINGSPUNKT = 'VURDERBV.UT'
WHERE NAVN IN (
  'Manuell vurdering av omsorgsvilkåret', -- Blir denne riktig?
  'Manuell vurdering av foreldreansvarsvilkåret 1',
  'Manuell vurdering av foreldreansvarsvilkåret 2'
);

-- Nye aksjonspunkter
INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, AKSJONSPUNKT_KATEGORI, VILKAR_TYPE, VURDERINGSPUNKT, ALLTID_TOTRINNSBEHANDLING, BESKRIVELSE)
VALUES ('5019', 'Avklar lovlig opphold.', 'APK-004', 'FP_VK_2', 'VURDERMV.INN', 'N', 'Avklar lovlig opphold.');
INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, AKSJONSPUNKT_KATEGORI, VILKAR_TYPE, VURDERINGSPUNKT, ALLTID_TOTRINNSBEHANDLING, BESKRIVELSE)
VALUES ('5020', 'Avklar om bruker er bosatt.', 'APK-004', 'FP_VK_2', 'VURDERMV.INN', 'N', 'Avklar om bruker er bosatt.');
INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, AKSJONSPUNKT_KATEGORI, VILKAR_TYPE, VURDERINGSPUNKT, ALLTID_TOTRINNSBEHANDLING, BESKRIVELSE)
VALUES ('5021', 'Avklar om bruker har gyldig periode.', 'APK-004', 'FP_VK_2', 'VURDERMV.INN', 'N', 'Avklar om bruker har gyldig periode.');
INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, AKSJONSPUNKT_KATEGORI, VILKAR_TYPE, VURDERINGSPUNKT, ALLTID_TOTRINNSBEHANDLING, BESKRIVELSE)
VALUES ('5022', 'Avklar fakta for status på person.', 'APK-004', 'FP_VK_2', 'VURDERMV.INN', 'N', 'Avklar fakta for status på person.');
INSERT INTO AKSJONSPUNKT_DEF(KODE, NAVN, AKSJONSPUNKT_KATEGORI, VILKAR_TYPE, VURDERINGSPUNKT, ALLTID_TOTRINNSBEHANDLING, BESKRIVELSE)
VALUES ('5023', 'Avklar oppholdsrett.', 'APK-004', 'FP_VK_2', 'VURDERMV.INN', 'N', 'Avklar oppholdsrett.');

/* ENDRINGER I PROSESSMODELL - SLUTT */

