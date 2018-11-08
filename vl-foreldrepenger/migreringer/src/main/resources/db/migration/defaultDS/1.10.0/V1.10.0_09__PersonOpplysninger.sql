-- ------------------------------- --
-- OPPLYSNINGSKILDE                --
-- ------------------------------- --
CREATE TABLE OPPLYSNINGSKILDE (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_OPPLYSNINGSKILDE PRIMARY KEY (kode)
);

INSERT INTO OPPLYSNINGSKILDE(kode, navn) VALUES ('UDEFINERT', 'Udefinert kilde');
INSERT INTO OPPLYSNINGSKILDE(kode, navn) VALUES ('TPS', 'TPS');
INSERT INTO OPPLYSNINGSKILDE(kode, navn) VALUES ('SAKSBEH', 'Saksbehandler');

COMMENT ON TABLE OPPLYSNINGSKILDE  IS '';
COMMENT ON COLUMN OPPLYSNINGSKILDE.KODE IS 'Kodeverk Primary Key';
COMMENT ON COLUMN OPPLYSNINGSKILDE.BESKRIVELSE IS 'Utdypende beskrivelse av koden';


-- ------------------------------- --
-- OPPLYSNING_ADRESSE_TYPE         --
-- ------------------------------- --
CREATE TABLE OPPLYSNING_ADRESSE_TYPE (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_OPPLYSNING_ADRESSE_TYPE PRIMARY KEY (kode)
);

INSERT INTO OPPLYSNING_ADRESSE_TYPE(kode, navn) VALUES ('BOAD', 'Bostedsadresse');
INSERT INTO OPPLYSNING_ADRESSE_TYPE(kode, navn) VALUES ('POST', 'Postadresse');
INSERT INTO OPPLYSNING_ADRESSE_TYPE(kode, navn) VALUES ('PUTL', 'Utenlandsk postadresse');
INSERT INTO OPPLYSNING_ADRESSE_TYPE(kode, navn) VALUES ('TIAD', 'Norsk NAV tilleggsadresse');
INSERT INTO OPPLYSNING_ADRESSE_TYPE(kode, navn) VALUES ('UTAD', 'Utenlandsk NAV tilleggsadresse');
INSERT INTO OPPLYSNING_ADRESSE_TYPE(kode, navn) VALUES ('UKJE', 'Ukjent adresse');



COMMENT ON TABLE OPPLYSNING_ADRESSE_TYPE  IS '';
COMMENT ON COLUMN OPPLYSNING_ADRESSE_TYPE.KODE IS 'Kodeverk Primary Key';
COMMENT ON COLUMN OPPLYSNING_ADRESSE_TYPE.BESKRIVELSE IS 'Utdypende beskrivelse av koden';


-- ------------------------------- --
-- RELASJONSROLLE_TYPE             --
-- ------------------------------- --
CREATE TABLE RELASJONSROLLE_TYPE (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_RELASJONSROLLE_TYPE PRIMARY KEY (kode)
);

INSERT INTO RELASJONSROLLE_TYPE(kode, navn) VALUES ('EKTE', 'Eftefelle til');
INSERT INTO RELASJONSROLLE_TYPE(kode, navn) VALUES ('BARN', 'Barn av');
INSERT INTO RELASJONSROLLE_TYPE(kode, navn) VALUES ('FARA', 'Far til');
INSERT INTO RELASJONSROLLE_TYPE(kode, navn) VALUES ('MORA', 'Mor til');
INSERT INTO RELASJONSROLLE_TYPE(kode, navn) VALUES ('REPA', 'Registrert partner med');
INSERT INTO RELASJONSROLLE_TYPE(kode, navn) VALUES ('SAMB', 'Samboer med');
INSERT INTO RELASJONSROLLE_TYPE(kode, navn) VALUES ('MEDS', 'Medsøker fra søknad');
INSERT INTO RELASJONSROLLE_TYPE(kode, navn) VALUES ('HOVS', 'Hovedsøker fra søknad');


COMMENT ON TABLE RELASJONSROLLE_TYPE  IS '';
COMMENT ON COLUMN RELASJONSROLLE_TYPE.KODE IS 'Kodeverk Primary Key';
COMMENT ON COLUMN RELASJONSROLLE_TYPE.BESKRIVELSE IS 'Utdypende beskrivelse av koden';


-- ------------------------------- --
-- DISKRESJONSKODE                 --
-- ------------------------------- --
CREATE TABLE DISKRESJONSKODE (
kode                           VARCHAR2(20 CHAR) NOT NULL,
navn                           VARCHAR2(50 CHAR) NOT NULL,
beskrivelse                    VARCHAR2(2000 CHAR),
opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
endret_av                      VARCHAR2(20 CHAR),
endret_tid                     TIMESTAMP(3),
CONSTRAINT PK_DISKRESJONSKODE PRIMARY KEY (kode)
);

INSERT INTO DISKRESJONSKODE(kode, navn) VALUES ('KLIE', 'Klientadresse');
INSERT INTO DISKRESJONSKODE(kode, navn) VALUES ('MILI', 'Militær');
INSERT INTO DISKRESJONSKODE(kode, navn) VALUES ('PEND', 'Pendler');
INSERT INTO DISKRESJONSKODE(kode, navn) VALUES ('SPFO', 'Sperret adresse, fortrolig');
INSERT INTO DISKRESJONSKODE(kode, navn) VALUES ('SPSF', 'Sperret adresse, strengt fortrolig');
INSERT INTO DISKRESJONSKODE(kode, navn) VALUES ('SVAL', 'Svalbard');
INSERT INTO DISKRESJONSKODE(kode, navn) VALUES ('UFB',  'Uten fast bopel');
INSERT INTO DISKRESJONSKODE(kode, navn) VALUES ('URIK', 'I utenrikstjeneste');
INSERT INTO DISKRESJONSKODE(kode, navn) VALUES ('UDEF', 'Udefinert');


COMMENT ON TABLE DISKRESJONSKODE  IS '';
COMMENT ON COLUMN DISKRESJONSKODE.KODE IS 'Kodeverk Primary Key';
COMMENT ON COLUMN DISKRESJONSKODE.BESKRIVELSE IS 'Utdypende beskrivelse av koden';

-- ------------------------------- --
-- SIVILSTAND_TYPE                 --
-- ------------------------------- --
CREATE TABLE SIVILSTAND_TYPE (
  kode                           VARCHAR2(20 CHAR) NOT NULL,
  navn                           VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse                    VARCHAR2(2000 CHAR),
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_SIVILSTAND_TYPE PRIMARY KEY (kode)
);

INSERT INTO SIVILSTAND_TYPE(kode, navn) VALUES ('ENKE', 'Enke/-mann');
INSERT INTO SIVILSTAND_TYPE(kode, navn) VALUES ('GIFT', 'Gift');
INSERT INTO SIVILSTAND_TYPE(kode, navn) VALUES ('GJPA', 'Gjenlevende partner');
INSERT INTO SIVILSTAND_TYPE(kode, navn) VALUES ('GLAD', 'Gift, lever adskilt');
INSERT INTO SIVILSTAND_TYPE(kode, navn) VALUES ('NULL', 'Uoppgitt');
INSERT INTO SIVILSTAND_TYPE(kode, navn) VALUES ('REPA', 'Registrert partner');
INSERT INTO SIVILSTAND_TYPE(kode, navn) VALUES ('SAMB', 'Samboer');
INSERT INTO SIVILSTAND_TYPE(kode, navn) VALUES ('SEPA', 'Separert partner');
INSERT INTO SIVILSTAND_TYPE(kode, navn) VALUES ('SEPR', 'Separert');
INSERT INTO SIVILSTAND_TYPE(kode, navn) VALUES ('SKIL', 'Skilt');
INSERT INTO SIVILSTAND_TYPE(kode, navn) VALUES ('SKPA', 'Skilt partner');
INSERT INTO SIVILSTAND_TYPE(kode, navn) VALUES ('UGIF', 'Ugift');

COMMENT ON TABLE SIVILSTAND_TYPE  IS '';
COMMENT ON COLUMN SIVILSTAND_TYPE.kode IS 'Kodeverk Primary Key';
COMMENT ON COLUMN SIVILSTAND_TYPE.beskrivelse IS 'Utdypende beskrivelse av koden';

-- ------------------------------- --
-- VALGT_OPPLYSNING                --
-- ------------------------------- --
CREATE TABLE VALGT_OPPLYSNING (
  id                        NUMBER(19) NOT NULL,
  bruker_kjoenn             VARCHAR2(7 CHAR) NOT NULL,
  statsborgerskap           VARCHAR2(20 CHAR) NOT NULL,
  personstatus_type         VARCHAR2(20 CHAR) NOT NULL,
  opprettet_av              VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid             TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                 VARCHAR2(20 CHAR),
  endret_tid                TIMESTAMP(3),
  CONSTRAINT PK_VALGT_OPPLYSNING   PRIMARY KEY (id),
  CONSTRAINT FK_VALGT_OPPLYSNING_1 FOREIGN KEY (bruker_kjoenn)      REFERENCES BRUKER_KJOENN,
  CONSTRAINT FK_VALGT_OPPLYSNING_2 FOREIGN KEY (statsborgerskap)    REFERENCES LANDKODER,
  CONSTRAINT FK_VALGT_OPPLYSNING_3 FOREIGN KEY (personstatus_type)  REFERENCES PERSONSTATUS_TYPE
);

CREATE INDEX IDX_VALGT_OPPLYSNING_1 ON VALGT_OPPLYSNING(bruker_kjoenn);
CREATE INDEX IDX_VALGT_OPPLYSNING_2 ON VALGT_OPPLYSNING(statsborgerskap);
CREATE INDEX IDX_VALGT_OPPLYSNING_3 ON VALGT_OPPLYSNING(personstatus_type);

CREATE SEQUENCE SEQ_VALGT_OPPLYSNING MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE VALGT_OPPLYSNING                     IS '';
COMMENT ON COLUMN VALGT_OPPLYSNING.bruker_kjoenn      IS 'Fremmednøkkel til valgt kjoenn';
COMMENT ON COLUMN VALGT_OPPLYSNING.statsborgerskap    IS 'Fremmednøkkel til valgt statsborgerskap';
COMMENT ON COLUMN VALGT_OPPLYSNING.personstatus_type  IS 'Fremmednøkkel til valgt personstatus';


-- ------------------------------- --
-- PERSONOPPLYSNING                --
-- ------------------------------- --
CREATE TABLE PERSONOPPLYSNING (
  id                             NUMBER(19) NOT NULL,
  nummer                         NUMBER(19),
  behandling_grunnlag_id         NUMBER(19),
  bruker_kjoenn                  VARCHAR2(7) NOT NULL,
  statsborgerskap                VARCHAR(20) NOT NULL,
  personstatus_type              VARCHAR(20) NOT NULL,
  overstyrt_personstatus_type    VARCHAR(20) NOT NULL,
  opplysningskilde               VARCHAR2(20 CHAR) NOT NULL ,
  region                         VARCHAR(20) NOT NULL,
  diskresjonskode                VARCHAR(20) NOT NULL,
  sivilstand_type                VARCHAR(20) NOT NULL,
  valgt_opplysning_id            NUMBER(19),
  aktoer_id                      NUMBER(19),
  navn                           VARCHAR2(100),
  doedsdato                      DATE,
  foedselsdato                   DATE,
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_PERSONOPPLYSNING PRIMARY KEY (id),
  CONSTRAINT FK_PERSONOPPLYSNING_1  FOREIGN KEY (behandling_grunnlag_id)      REFERENCES BEHANDLING_GRUNNLAG,
  CONSTRAINT FK_PERSONOPPLYSNING_2  FOREIGN KEY (bruker_kjoenn)               REFERENCES BRUKER_KJOENN,
  CONSTRAINT FK_PERSONOPPLYSNING_3  FOREIGN KEY (statsborgerskap)             REFERENCES LANDKODER,
  CONSTRAINT FK_PERSONOPPLYSNING_4  FOREIGN KEY (personstatus_type)           REFERENCES PERSONSTATUS_TYPE,
  CONSTRAINT FK_PERSONOPPLYSNING_5  FOREIGN KEY (overstyrt_personstatus_type) REFERENCES PERSONSTATUS_TYPE,
  CONSTRAINT FK_PERSONOPPLYSNING_6  FOREIGN KEY (diskresjonskode)             REFERENCES DISKRESJONSKODE,
  CONSTRAINT FK_PERSONOPPLYSNING_7  FOREIGN KEY (sivilstand_type)             REFERENCES SIVILSTAND_TYPE,
  CONSTRAINT FK_PERSONOPPLYSNING_8  FOREIGN KEY (valgt_opplysning_id)         REFERENCES VALGT_OPPLYSNING,
  CONSTRAINT FK_PERSONOPPLYSNING_9  FOREIGN KEY (region)                      REFERENCES REGION,
  CONSTRAINT FK_PERSONOPPLYSNING_10 FOREIGN KEY (opplysningskilde)            REFERENCES OPPLYSNINGSKILDE

);

CREATE UNIQUE INDEX UIDX_PERSONOPPLYSNING_1 ON PERSONOPPLYSNING(behandling_grunnlag_id);
CREATE INDEX IDX_PERSONOPPLYSNING_1 ON PERSONOPPLYSNING(bruker_kjoenn);
CREATE INDEX IDX_PERSONOPPLYSNING_2 ON PERSONOPPLYSNING(statsborgerskap);
CREATE INDEX IDX_PERSONOPPLYSNING_3 ON PERSONOPPLYSNING(personstatus_type);
CREATE INDEX IDX_PERSONOPPLYSNING_4 ON PERSONOPPLYSNING(overstyrt_personstatus_type);
CREATE INDEX IDX_PERSONOPPLYSNING_5 ON PERSONOPPLYSNING(diskresjonskode);
CREATE INDEX IDX_PERSONOPPLYSNING_6 ON PERSONOPPLYSNING(sivilstand_type);
CREATE INDEX IDX_PERSONOPPLYSNING_7 ON PERSONOPPLYSNING(valgt_opplysning_id);
CREATE INDEX IDX_PERSONOPPLYSNING_8 ON PERSONOPPLYSNING(region);
CREATE INDEX IDX_PERSONOPPLYSNING_9 ON PERSONOPPLYSNING(opplysningskilde);


CREATE SEQUENCE SEQ_PERSONOPPLYSNING MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE PERSONOPPLYSNING  IS '';
COMMENT ON COLUMN PERSONOPPLYSNING.behandling_grunnlag_id IS '';
COMMENT ON COLUMN PERSONOPPLYSNING.bruker_kjoenn IS '';
COMMENT ON COLUMN PERSONOPPLYSNING.statsborgerskap IS '';
COMMENT ON COLUMN PERSONOPPLYSNING.personstatus_type IS '';
COMMENT ON COLUMN PERSONOPPLYSNING.overstyrt_personstatus_type IS '';
COMMENT ON COLUMN PERSONOPPLYSNING.diskresjonskode IS '';
COMMENT ON COLUMN PERSONOPPLYSNING.sivilstand_type IS '';
COMMENT ON COLUMN PERSONOPPLYSNING.valgt_opplysning_id IS '';
COMMENT ON COLUMN PERSONOPPLYSNING.opplysningskilde IS '';
COMMENT ON COLUMN PERSONOPPLYSNING.aktoer_id IS '';
COMMENT ON COLUMN PERSONOPPLYSNING.navn IS '';
COMMENT ON COLUMN PERSONOPPLYSNING.doedsdato IS '';


-- ------------------------------- --
-- OPPLYSNING_ADRESSE              --
-- ------------------------------- --
CREATE TABLE OPPLYSNING_ADRESSE (
  id                             NUMBER(19) NOT NULL,
  adresse_id                     NUMBER(19),
  personopplysning_id            NUMBER(19),
  adresse_type                   VARCHAR2(20) NOT NULL ,
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_OPPLYSNING_ADRESSE PRIMARY KEY (id),
  CONSTRAINT FK_OPPLYSNING_ADRESSE_1 FOREIGN KEY (adresse_id)          REFERENCES DOKUMENT_ADRESSE,
  CONSTRAINT FK_OPPLYSNING_ADRESSE_2 FOREIGN KEY (personopplysning_id) REFERENCES PERSONOPPLYSNING,
  CONSTRAINT FK_OPPLYSNING_ADRESSE_3 FOREIGN KEY (adresse_type)        REFERENCES OPPLYSNING_ADRESSE_TYPE
);

CREATE INDEX IDX_OPPLYSNING_ADRESSE_1 ON OPPLYSNING_ADRESSE(adresse_id);
CREATE INDEX IDX_OPPLYSNING_ADRESSE_2 ON OPPLYSNING_ADRESSE(personopplysning_id);
CREATE INDEX IDX_OPPLYSNING_ADRESSE_3 ON OPPLYSNING_ADRESSE(adresse_type);

CREATE SEQUENCE SEQ_OPPLYSNING_ADRESSE MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE OPPLYSNING_ADRESSE                      IS '';
COMMENT ON COLUMN OPPLYSNING_ADRESSE.adresse_id          IS 'Fremmednøkkel til adressen';
COMMENT ON COLUMN OPPLYSNING_ADRESSE.personopplysning_id IS 'Fremmednøkkel til personopplysning som denne adressen hører til';
COMMENT ON COLUMN OPPLYSNING_ADRESSE.adresse_type        IS 'Fremmednøkkel som angir vilken type av adresse dette er';




-- ------------------------------- --
-- FAMILIERELASJON                 --
-- ------------------------------- --
CREATE TABLE FAMILIERELASJON (
  id                             NUMBER(19) NOT NULL,
  fraPerson                      NUMBER(19),
  tilPerson                      NUMBER(19),
  relasjonsrolle                 VARCHAR2(20 CHAR) NOT NULL ,
  opprettet_av                   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid                  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                      VARCHAR2(20 CHAR),
  endret_tid                     TIMESTAMP(3),
  CONSTRAINT PK_FAMILIERELASJON PRIMARY KEY (id),
  CONSTRAINT FK_FAMILIERELASJON_1 FOREIGN KEY (fraPerson)        REFERENCES PERSONOPPLYSNING,
  CONSTRAINT FK_FAMILIERELASJON_2 FOREIGN KEY (tilPerson)        REFERENCES PERSONOPPLYSNING,
  CONSTRAINT FK_FAMILIERELASJON_3 FOREIGN KEY (relasjonsrolle)   REFERENCES RELASJONSROLLE_TYPE
);

CREATE INDEX IDX_FAMILIERELASJON_1 ON FAMILIERELASJON(fraPerson);
CREATE INDEX IDX_FAMILIERELASJON_2 ON FAMILIERELASJON(tilPerson);
CREATE INDEX IDX_FAMILIERELASJON_3 ON FAMILIERELASJON(relasjonsrolle);

CREATE SEQUENCE SEQ_FAMILIERELASJON MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE FAMILIERELASJON                 IS '';
COMMENT ON COLUMN FAMILIERELASJON.fraPerson      IS 'Fremmednøkkel for "fra"-relasjon';
COMMENT ON COLUMN FAMILIERELASJON.tilPerson      IS 'Fremmednøkkel for "til"-relasjon';
COMMENT ON COLUMN FAMILIERELASJON.relasjonsRolle IS 'Viser vilken relasjon fraPerson har til tilPerson';




