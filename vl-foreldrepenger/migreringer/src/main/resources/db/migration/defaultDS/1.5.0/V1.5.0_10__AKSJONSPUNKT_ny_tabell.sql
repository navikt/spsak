-- Tabell AKSJONSPUNKT_KATEGORI
CREATE TABLE AKSJONSPUNKT_KATEGORI (
    kode VARCHAR2(20 char) not null,
    navn varchar2(40 char) not null,
    beskrivelse varchar2(4000 CHAR),
    opprettet_av    VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 char),
    endret_tid      TIMESTAMP(3),
	  CONSTRAINT PK_AKSJONSPUNKT_KATEGORI PRIMARY KEY (KODE)
);

INSERT INTO AKSJONSPUNKT_KATEGORI (kode, navn) VALUES ('APK-001', 'Registrering');
INSERT INTO AKSJONSPUNKT_KATEGORI (kode, navn) VALUES ('APK-002', 'Vurdering');
INSERT INTO AKSJONSPUNKT_KATEGORI (kode, navn) VALUES ('APK-003', 'Kontroll');

-- Tabell AKSJONSPUNKT_STATUS
CREATE TABLE AKSJONSPUNKT_STATUS (
    kode VARCHAR2(20 char) not null,
    navn varchar2(40 char) not null,
    beskrivelse varchar2(4000 char),
    opprettet_av    VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 char),
    endret_tid      TIMESTAMP(3),
	  CONSTRAINT PK_AKSJONSPUNKT_STATUS PRIMARY KEY (KODE)
);

INSERT INTO AKSJONSPUNKT_STATUS (kode, navn) VALUES ('OPPR', 'Opprettet');
INSERT INTO AKSJONSPUNKT_STATUS (kode, navn) VALUES ('FORD', 'Fordelt');
INSERT INTO AKSJONSPUNKT_STATUS (kode, navn) VALUES ('UTFO', 'Utført');
INSERT INTO AKSJONSPUNKT_STATUS (kode, navn) VALUES ('REAP', 'Reåpnet');
INSERT INTO AKSJONSPUNKT_STATUS (kode, navn) VALUES ('LUKK', 'Lukket');

-- Tabell AKSJONSPUNKT_DEF
CREATE TABLE AKSJONSPUNKT_DEF (
    kode VARCHAR2(50 char) not null,
    navn varchar2(50 char) not null,
    aksjonspunkt_kategori VARCHAR2(20 char) not null,
    vurderingspunkt VARCHAR2(20 char) not null,
    beskrivelse varchar2(4000 char),
    opprettet_av    VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 char),
    endret_tid      TIMESTAMP(3),
	  CONSTRAINT PK_AKSJONSPUNKT_DEF PRIMARY KEY (kode),
	  CONSTRAINT FK_AKSJONSPUNKT_DEF_1 FOREIGN KEY (aksjonspunkt_kategori) REFERENCES AKSJONSPUNKT_KATEGORI,
	  CONSTRAINT FK_AKSJONSPUNKT_DEF_2 FOREIGN KEY (vurderingspunkt) REFERENCES VURDERINGSPUNKT_DEF
);

CREATE INDEX IDX_AKSJONSPUNKT_DEF_1 ON AKSJONSPUNKT_DEF(aksjonspunkt_kategori);
CREATE INDEX IDX_AKSJONSPUNKT_DEF_2 ON AKSJONSPUNKT_DEF(vurderingspunkt);

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, AKSJONSPUNKT_KATEGORI, VURDERINGSPUNKT, BESKRIVELSE) VALUES ('FP_VK_1.AP_1', 'Kontroller terminbekreftelse', 'APK-003', 'VUIN.UT', 'Kontroller terminbekreftelsen og gjør eventuelle endringer før du bekrefter');
INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, AKSJONSPUNKT_KATEGORI, VURDERINGSPUNKT, BESKRIVELSE) VALUES ('FP_VK_1.AP_2', 'Kontroller fødsel', 'APK-003', 'VUIN.UT', 'Kontroller fødselsdokumentasjonen og gjør eventuelle endringer før du bekrefter');
INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, AKSJONSPUNKT_KATEGORI, VURDERINGSPUNKT, BESKRIVELSE) VALUES ('FP_VK_1.AP_3', 'Kontroller antall barn', 'APK-003', 'VUIN.UT', 'Kontroller antall barn og gjør eventuelle endringer før du bekrefter');

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, AKSJONSPUNKT_KATEGORI, VURDERINGSPUNKT, BESKRIVELSE) VALUES ('FP_VK_2.AP_1', 'Manuell vurdering av medlemskap', 'APK-002', 'VUIN.UT', 'MISSING');

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, AKSJONSPUNKT_KATEGORI, VURDERINGSPUNKT, BESKRIVELSE) VALUES ('FP_VK_3.AP_1', 'Vurder utfall av søknadsfristvilkåret', 'APK-003', 'VUIN.UT', 'Vurder mulige unntak ved brudd på søknadsfrist');

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, AKSJONSPUNKT_KATEGORI, VURDERINGSPUNKT, BESKRIVELSE) VALUES ('FP_VK_4.AP_1', 'Kontroller dokumentasjon av adopsjon', 'APK-003', 'VUIN.UT', 'Kontroller adopsjonsdokumentasjon og gjør eventuelle endringer');
INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, AKSJONSPUNKT_KATEGORI, VURDERINGSPUNKT, BESKRIVELSE) VALUES ('FP_VK_4.AP_2', 'Kontroll om adopsjon gjelder ektefelles barn', 'APK-003', 'VUIN.UT', 'Vurder om dette er ektefelles barn');
INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, AKSJONSPUNKT_KATEGORI, VURDERINGSPUNKT, BESKRIVELSE) VALUES ('FP_VK_4.AP_3', 'Kontrollerer om søker er mann adopterer alene', 'APK-003', 'VUIN.UT', 'Vurder om søker adopterer alene');

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, AKSJONSPUNKT_KATEGORI, VURDERINGSPUNKT, BESKRIVELSE) VALUES ('AF.AP_1', 'Avklar fakta for omsorgs/foreldreansvarsvilkåret', 'APK-003', 'VUIN.UT', 'Kontroller opplysningene og gjør eventuelle endringer før du bekrefter');


-- Tabell AKSJONSPUNKT
CREATE TABLE AKSJONSPUNKT (
	id number(19) NOT NULL,
	behandling_steg_funnet varchar2(20 char),
	behandling_resultat_id number(19) NOT NULL,
	aksjonspunkt_status VARCHAR2(7 char) NOT NULL,
	aksjonspunkt_def VARCHAR2(50 char) NOT NULL,
	periode_fom DATE,
  periode_tom DATE,
  begrunnelse VARCHAR2(4000 char),
  versjon              NUMBER(19) DEFAULT 0 NOT NULL,
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
	CONSTRAINT PK_AKSJONSPUNKT PRIMARY KEY (ID),
	CONSTRAINT FK_AKSJONSPUNKT_1 FOREIGN KEY (aksjonspunkt_def) REFERENCES AKSJONSPUNKT_DEF,
	CONSTRAINT FK_AKSJONSPUNKT_2 FOREIGN KEY (aksjonspunkt_status) REFERENCES AKSJONSPUNKT_STATUS,
	CONSTRAINT FK_AKSJONSPUNKT_3 FOREIGN KEY (behandling_steg_funnet) REFERENCES BEHANDLING_STEG_TYPE,
	CONSTRAINT FK_AKSJONSPUNKT_4 FOREIGN KEY (behandling_resultat_id) REFERENCES BEHANDLING_RESULTAT
);

CREATE SEQUENCE SEQ_AKSJONSPUNKT MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE INDEX IDX_AKSJONSPUNKT_1 ON AKSJONSPUNKT(aksjonspunkt_def);
CREATE INDEX IDX_AKSJONSPUNKT_2 ON AKSJONSPUNKT(aksjonspunkt_status);
CREATE INDEX IDX_AKSJONSPUNKT_3 ON AKSJONSPUNKT(behandling_steg_funnet);

-- Tabell REGEL_MERKNAD_DEF
CREATE TABLE REGEL_MERKNAD_DEF (
    KODE VARCHAR2(20 char) not null,
    NAVN varchar2(50 char)not null,
    AKSJONSPUNKT_DEF VARCHAR2(50 char),
    beskrivelse varchar2(4000 char),
    opprettet_av    VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 char),
    endret_tid      TIMESTAMP(3),
	CONSTRAINT PK_REGEL_MERKNAD_DEF PRIMARY KEY (KODE)
);

CREATE SEQUENCE SEQ_REGEL_MERKNAD MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

ALTER TABLE REGEL_MERKNAD_DEF ADD CONSTRAINT FK_REGEL_MERKNAD_DEF_1 FOREIGN KEY (aksjonspunkt_def) REFERENCES aksjonspunkt_def;

CREATE INDEX IDX_REGEL_MERKNAD_DEF_1 ON REGEL_MERKNAD_DEF(aksjonspunkt_def);

INSERT INTO REGEL_MERKNAD_DEF (KODE, NAVN, AKSJONSPUNKT_DEF, BESKRIVELSE) VALUES ('5001', 'Kontroller terminbekreftelse', 'FP_VK_1.AP_1', 'Kontroller terminbekreftelsen og gjør eventuelle endringer før du bekrefter');
INSERT INTO REGEL_MERKNAD_DEF (KODE, NAVN, AKSJONSPUNKT_DEF, BESKRIVELSE) VALUES ('5002', 'Kontroller fødselsdokumentasjonen', 'FP_VK_1.AP_2', 'Kontroller fødselsdokumentasjonen og gjør eventuelle endringer før du bekrefter');
INSERT INTO REGEL_MERKNAD_DEF (KODE, NAVN, AKSJONSPUNKT_DEF, BESKRIVELSE) VALUES ('5003', 'Kontroller antall barn', 'FP_VK_1.AP_3', 'Kontroller antall barn og gjør eventuelle endringer før du bekrefter');
INSERT INTO REGEL_MERKNAD_DEF (KODE, NAVN, AKSJONSPUNKT_DEF, BESKRIVELSE) VALUES ('5004', 'Kontroller adopsjonsdok', 'FP_VK_4.AP_1', 'Kontroller adopsjonsdokumentasjon og gjør eventuelle endringer');
INSERT INTO REGEL_MERKNAD_DEF (KODE, NAVN, AKSJONSPUNKT_DEF, BESKRIVELSE) VALUES ('5005', 'Kontroller ektefelles barn', 'FP_VK_4.AP_2', 'Vurder om dette er ektefelles barn');
INSERT INTO REGEL_MERKNAD_DEF (KODE, NAVN, AKSJONSPUNKT_DEF, BESKRIVELSE) VALUES ('5006', 'Kontroller adopterer alene', 'FP_VK_4.AP_3', 'Vurder om søker adopterer alene');
INSERT INTO REGEL_MERKNAD_DEF (KODE, NAVN, AKSJONSPUNKT_DEF, BESKRIVELSE) VALUES ('5007', 'Vurder utfall av søknadsfristvilkåret', 'FP_VK_3.AP_1', 'Kontroller opplysningene og gjør eventuelle endringer før du bekrefter');
INSERT INTO REGEL_MERKNAD_DEF (KODE, NAVN, AKSJONSPUNKT_DEF, BESKRIVELSE) VALUES ('5008', 'Avklar fakta for omsorgs/foreldreansvarsvilkåret', 'AF.AP_1', 'Vurder mulige unntak ved brudd på søknadsfrist');


-- Tabell REGEL_MERKNAD
CREATE TABLE REGEL_MERKNAD (
    id number(19, 0) not null,
    aksjonspunkt_id NUMBER(19, 0) NOT NULL,
	  regel_merknad_def VARCHAR2(20 char) not null,
    versjon              NUMBER(19, 0) DEFAULT 0 NOT NULL,
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
	  CONSTRAINT PK_REGEL_MERKNAD PRIMARY KEY (id),
	  CONSTRAINT FK_REGEL_MERKNAD_1 FOREIGN KEY (regel_merknad_def) REFERENCES REGEL_MERKNAD_DEF,
	  CONSTRAINT FK_REGEL_MERKNAD_2 FOREIGN KEY (aksjonspunkt_id) REFERENCES AKSJONSPUNKT
);

CREATE INDEX IDX_REGEL_MERKNAD_1 ON REGEL_MERKNAD(aksjonspunkt_id);
CREATE INDEX IDX_REGEL_MERKNAD_2 ON REGEL_MERKNAD(regel_merknad_def);

