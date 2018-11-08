-- Tabell VILKAR_TYPE
CREATE TABLE VILKAR_TYPE (
    kode varchar2(20 char) not null,
    navn varchar2(50 char) not null,
    beskrivelse varchar2(4000 char),
    opprettet_av    VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 char),
    endret_tid      TIMESTAMP(3),
	CONSTRAINT PK_VILKAR_TYPE PRIMARY KEY (kode)
);

INSERT INTO VILKAR_TYPE (kode, navn) VALUES ('FP_VK_1', 'Fødselsvilkåret');
INSERT INTO VILKAR_TYPE (kode, navn) VALUES ('FP_VK_2', 'Medlemskapsvilkåret');
INSERT INTO VILKAR_TYPE (kode, navn) VALUES ('FP_VK_3', 'Søknadsfristvilkåret');
INSERT INTO VILKAR_TYPE (kode, navn) VALUES ('FP_VK_4', 'Adopsjonsvilkåret');
INSERT INTO VILKAR_TYPE (kode, navn) VALUES ('FP_VK_5', 'Omsorgsvilkåret');
INSERT INTO VILKAR_TYPE (kode, navn) VALUES ('FP_VK_6', 'Samordning med sykepenger');
INSERT INTO VILKAR_TYPE (kode, navn) VALUES ('FP_VK_7', 'Samordningsvilkåret FP');
INSERT INTO VILKAR_TYPE (kode, navn) VALUES ('FP_VK_8', 'Foreldreansvarsvilkåret');

CREATE TABLE VILKAR_UTFALL_TYPE (
    kode varchar2(30 CHAR) not null,
    navn varchar2(50 CHAR)not null,
    beskrivelse varchar2(4000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
	CONSTRAINT PK_VILKAR_UTFALL_TYPE PRIMARY KEY (kode)
);

INSERT INTO VILKAR_UTFALL_TYPE (kode, navn) VALUES ('OPPFYLT', 'Vilkåret er oppfylt');
INSERT INTO VILKAR_UTFALL_TYPE (kode, navn) VALUES ('IKKE_OPPFYLT', 'Vilkåret er ikke oppfylt');
INSERT INTO VILKAR_UTFALL_TYPE (kode, navn) VALUES ('IKKE_RELEVANT', 'Vilkåret er ikke relevant');
INSERT INTO VILKAR_UTFALL_TYPE (kode, navn) VALUES ('IKKE_VURDERT', 'Vilkåret er ikke vurdert');
INSERT INTO VILKAR_UTFALL_TYPE (kode, navn) VALUES ('AVSLAAS_I_ANNET_VILKAAR', 'Vilkåret avslås i annet vilkår');

-- Table VILKAR_RESULTAT_TYPE
CREATE TABLE VILKAR_RESULTAT_TYPE  (
    kode varchar2(20 CHAR) not null,
    navn varchar2(50 CHAR)not null,
    beskrivelse varchar2(4000 CHAR),
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
	CONSTRAINT PK_VILKAR_RESULTAT_TYPE PRIMARY KEY (KODE)
);

INSERT INTO VILKAR_RESULTAT_TYPE (kode, navn) VALUES ('INNVILGET', 'Innvilget');
INSERT INTO VILKAR_RESULTAT_TYPE (kode, navn) VALUES ('AVSLAATT', 'Avslått');
INSERT INTO VILKAR_RESULTAT_TYPE (kode, navn) VALUES ('IKKE_FASTSATT', 'Ikke fastsatt');

-- Table VILKAR_UTFALL_MERKNAD
CREATE TABLE VILKAR_UTFALL_MERKNAD (
  kode            VARCHAR2(7 char) NOT NULL,
  navn            VARCHAR2(50 CHAR) NOT NULL,
  opprettet_av    VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  beskrivelse     VARCHAR2(2000 CHAR),
  endret_av       VARCHAR2(20 char),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_VILKAR_UTFALL_MERKNAD PRIMARY KEY (KODE)
);
INSERT INTO VILKAR_UTFALL_MERKNAD (kode, navn, beskrivelse) VALUES ('1001', '1001', 'Søknad er sendt før 26. svangerskapsuke er passert og barnet er ikke født');
INSERT INTO VILKAR_UTFALL_MERKNAD (kode, navn, beskrivelse) VALUES ('1002', '1002', 'Søker er medmor (forelder2) og har søkt om engangsstønad til mor');
INSERT INTO VILKAR_UTFALL_MERKNAD (kode, navn, beskrivelse) VALUES ('1003', '1003', 'Søker er far og har søkt om engangsstønad til mor');
INSERT INTO VILKAR_UTFALL_MERKNAD (kode, navn, beskrivelse) VALUES ('1004', '1004', 'Barn over 15 år ved dato for omsorgsovertakelse');
INSERT INTO VILKAR_UTFALL_MERKNAD (kode, navn, beskrivelse) VALUES ('1005', '1005', 'Adopsjon av ektefellens barn');
INSERT INTO VILKAR_UTFALL_MERKNAD (kode, navn, beskrivelse) VALUES ('1006', '1006', 'Mann adopterer ikke alene');

INSERT INTO VILKAR_UTFALL_MERKNAD (kode, navn, beskrivelse) VALUES ('7001', '7001', 'Søker ikke oppfylt opplysningsplikten jf folketrygdloven §§ 21-7 og 21-3');
INSERT INTO VILKAR_UTFALL_MERKNAD (kode, navn, beskrivelse) VALUES ('7002', '7002', 'Start ny vilkårsvurdering');
INSERT INTO VILKAR_UTFALL_MERKNAD (kode, navn, beskrivelse) VALUES ('7003', '7003', 'Søker er medmor (foreldre2) og har søkt på vegne av seg selv');
INSERT INTO VILKAR_UTFALL_MERKNAD (kode, navn, beskrivelse) VALUES ('7004', '7004', 'Søker er far og har søkt på vegne av seg selv');



-- Tabell INNGANGSVILKAR_RESULTAT
CREATE TABLE INNGANGSVILKAR_RESULTAT (
   id NUMBER(19, 0) NOT NULL,
   vilkar_resultat varchar2(20 CHAR) default 'IKKE_FASTSATT' NOT NULL,
   original_behandling_id NUMBER(19, 0),
   versjon              NUMBER(19) DEFAULT 0 NOT NULL,
   opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
   opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
   endret_av       VARCHAR2(20 CHAR),
   endret_tid      TIMESTAMP(3),
   CONSTRAINT PK_INNGANGSVILKAR_RESULTAT PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_INNGANGSVILKAR_RESULTAT MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

ALTER TABLE INNGANGSVILKAR_RESULTAT ADD CONSTRAINT FK_INNGANGSVILKAR_RESULTAT_1 FOREIGN KEY (vilkar_resultat) REFERENCES VILKAR_RESULTAT_TYPE;
CREATE INDEX IDX_INNGANGSVILKAR_RES_1 ON INNGANGSVILKAR_RESULTAT(vilkar_resultat);
CREATE INDEX IDX_INNGANGSVILKAR_RES_2 ON INNGANGSVILKAR_RESULTAT(original_behandling_id);

-- Tabell VILKAR
CREATE TABLE VILKAR (
   id NUMBER(19, 0) NOT NULL,
   inngangsvilkar_resultat_id NUMBER(19, 0),
   vilkar_utfall varchar2(30 char),
   vilkar_type varchar2(7 char) not null,
   vilkar_utfall_merknad   VARCHAR2(7 char),
   versjon              NUMBER(19) DEFAULT 0 NOT NULL,
   opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
   opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
   endret_av       VARCHAR2(20 CHAR),
   endret_tid      TIMESTAMP(3),
   CONSTRAINT PK_VILKAR PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_VILKAR MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

ALTER TABLE VILKAR ADD CONSTRAINT FK_VILKAR_1 FOREIGN KEY (vilkar_utfall) REFERENCES VILKAR_UTFALL_TYPE;
ALTER TABLE VILKAR ADD CONSTRAINT FK_VILKAR_2 FOREIGN KEY (vilkar_type) REFERENCES VILKAR_TYPE;
ALTER TABLE VILKAR ADD CONSTRAINT FK_VILKAR_3 FOREIGN KEY (inngangsvilkar_resultat_id) REFERENCES INNGANGSVILKAR_RESULTAT;
ALTER TABLE VILKAR ADD CONSTRAINT FK_VILKAR_4 FOREIGN KEY (vilkar_utfall_merknad) REFERENCES VILKAR_UTFALL_MERKNAD;

CREATE INDEX IDX_VILKAR_1 ON VILKAR(vilkar_utfall);
CREATE INDEX IDX_VILKAR_2 ON VILKAR(vilkar_type);
CREATE INDEX IDX_VILKAR_3 ON VILKAR(inngangsvilkar_resultat_id);


-- Tabell BEREGNING_RESULTAT
CREATE TABLE BEREGNING_RESULTAT(
  id                       NUMBER(19, 0) NOT NULL,
  original_behandling_id    NUMBER(19) NOT NULL,
  versjon                  NUMBER(19) DEFAULT 0 NOT NULL,
  opprettet_av             VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
  opprettet_tid            TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                VARCHAR2(20 char),
  endret_tid               TIMESTAMP(3),
  CONSTRAINT PK_BEREGNING_RESULTAT PRIMARY KEY ( id )
);
CREATE SEQUENCE SEQ_BEREGNING_RESULTAT MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE INDEX IDX_BEREGNING_RESULTAT_1 ON BEREGNING_RESULTAT(original_behandling_id);

-- Tabell BEREGNING
CREATE TABLE BEREGNING(
  id                       NUMBER(19, 0) NOT NULL,
  sats_verdi               NUMBER(10, 0) NOT NULL,
  antall_barn              NUMBER(1, 0)  NOT NULL,
  beregnet_tilkjent_ytelse NUMBER(10, 0) NOT NULL,
  beregnet_dato            DATE NOT NULL,
  beregning_resultat_id    NUMBER(19) NOT NULL,
  versjon                  NUMBER(19) DEFAULT 0 NOT NULL,
  opprettet_av             VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
  opprettet_tid            TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                VARCHAR2(20 char),
  endret_tid               TIMESTAMP(3),
  CONSTRAINT PK_BEREGNING PRIMARY KEY ( id )
);
CREATE SEQUENCE SEQ_BEREGNING MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE INDEX IDX_BEREGNING_1 ON BEREGNING(beregning_resultat_id);
ALTER TABLE BEREGNING ADD CONSTRAINT FK_BEREGNING_1 FOREIGN KEY (beregning_resultat_id) REFERENCES BEREGNING_RESULTAT;


-- Tabell SATS_TYPE
CREATE TABLE SATS_TYPE (
  kode            VARCHAR2(7 char) NOT NULL,
  navn            VARCHAR2(50 CHAR) NOT NULL,
  opprettet_av    VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  beskrivelse     VARCHAR2(2000 CHAR),
  endret_av       VARCHAR2(20 char),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_SATS_TYPE PRIMARY KEY ( kode )
);
INSERT INTO SATS_TYPE (kode, navn) VALUES ('ENGANG', 'Engangsstønad');

-- Tabell SATS
CREATE TABLE SATS (
  id                         NUMBER(19) NOT NULL,
  sats_type                  VARCHAR2(7 char) NOT NULL,
  fom                        DATE NOT NULL,
  tom                        DATE NOT NULL,
  verdi                      NUMBER(10, 0) NOT NULL,
  versjon                    NUMBER(19) DEFAULT 0 NOT NULL,
  opprettet_av               VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
  opprettet_tid              TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av                  VARCHAR2(20 char),
  endret_tid                 TIMESTAMP(3),
  CONSTRAINT PK_SATS PRIMARY KEY ( id )
);
CREATE SEQUENCE SEQ_SATS MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE INDEX IDX_SATS_1 ON  SATS ( sats_type ASC );
ALTER TABLE SATS ADD CONSTRAINT FK_SATS_1 FOREIGN KEY ( sats_type ) REFERENCES SATS_TYPE ( kode );

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI, VERSJON, OPPRETTET_AV, OPPRETTET_TID)
VALUES (seq_sats.nextval, 'ENGANG', TO_DATE('2017-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 61120, 0, 'VL', SYSTIMESTAMP);


-- Tabell BEHANDLING_RESULTAT
CREATE TABLE BEHANDLING_RESULTAT (
  id NUMBER(19, 0) NOT NULL,
  behandling_id NUMBER(19, 0) NOT NULL,
  inngangsvilkar_resultat_id NUMBER(19, 0),
  beregning_resultat_id NUMBER(19, 0),
  behandling_resultat VARCHAR2(20 char) default 'IKKE_FASTSATT' NOT NULL,
  versjon              NUMBER(19) DEFAULT 0 NOT NULL,
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_BEHANDLING_RESULTAT PRIMARY KEY(id)
);
CREATE SEQUENCE SEQ_BEHANDLING_RESULTAT MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

ALTER TABLE BEHANDLING_RESULTAT ADD CONSTRAINT FK_BEHANDLING_RESULTAT_1 FOREIGN KEY (inngangsvilkar_resultat_id) REFERENCES INNGANGSVILKAR_RESULTAT;
CREATE INDEX IDX_BEHANDLING_RESULTAT_1 ON BEHANDLING_RESULTAT(inngangsvilkar_resultat_id);

ALTER TABLE BEHANDLING_RESULTAT ADD CONSTRAINT FK_BEHANDLING_RESULTAT_2 FOREIGN KEY (beregning_resultat_id) REFERENCES BEREGNING_RESULTAT;
CREATE INDEX IDX_BEHANDLING_RESULTAT_2 ON BEHANDLING_RESULTAT (beregning_resultat_id);

ALTER TABLE BEHANDLING_RESULTAT ADD CONSTRAINT FK_BEHANDLING_RESULTAT_3 FOREIGN KEY (behandling_id) REFERENCES BEHANDLING;
CREATE INDEX IDX_BEHANDLING_RESULTAT_3 ON BEHANDLING_RESULTAT(behandling_id);

ALTER TABLE BEHANDLING_RESULTAT ADD CONSTRAINT FK_BEHANDLING_RESULTAT_4 FOREIGN KEY (behandling_resultat) REFERENCES VILKAR_RESULTAT_TYPE;
CREATE INDEX IDX_BEHANDLING_RESULTAT_4 ON BEHANDLING_RESULTAT(behandling_resultat);

ALTER TABLE INNGANGSVILKAR_RESULTAT ADD CONSTRAINT FK_INNGANGSVILKAR_RESULTAT_2 FOREIGN KEY (original_behandling_id) REFERENCES BEHANDLING;
ALTER TABLE BEREGNING_RESULTAT ADD CONSTRAINT FK_BEREGNING_RESULTAT_1 FOREIGN KEY ( original_behandling_id ) REFERENCES BEHANDLING ( id );
