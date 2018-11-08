-- Tabell VENT_AARSAK
CREATE TABLE VENT_AARSAK (
  kode            VARCHAR2(20 CHAR) NOT NULL,
  navn            VARCHAR2(50 CHAR) NOT NULL,
  beskrivelse     VARCHAR2(2000 CHAR),
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_VENT_AARSAK PRIMARY KEY (kode)
);

COMMENT ON TABLE VENT_AARSAK IS 'Angir definerte årsaker for å sette en behandling på vent';
COMMENT ON COLUMN VENT_AARSAK.kode IS 'PK - unik kode som identifiserer årsak';
COMMENT ON COLUMN VENT_AARSAK.navn IS 'Lesbart navn for årsaken';
COMMENT ON COLUMN VENT_AARSAK.beskrivelse IS 'Utdypende forklaring av årsaken';

INSERT INTO VENT_AARSAK(kode, navn) VALUES ('AVV_DOK', 'Avventer dokumentasjon');
INSERT INTO VENT_AARSAK(kode, navn) VALUES ('AVV_FODSEL', 'Avventer fødsel');
INSERT INTO VENT_AARSAK(kode, navn) VALUES ('UTV_FRIST', 'Bruker har bedt om utvidet frist');

ALTER TABLE AKSJONSPUNKT ADD vent_aarsak VARCHAR2(20 CHAR);
COMMENT ON COLUMN AKSJONSPUNKT.vent_aarsak IS 'Årsak for at behandling er satt på vent';
ALTER TABLE AKSJONSPUNKT ADD CONSTRAINT FK_AKSJONSPUNKT_5 FOREIGN KEY (vent_aarsak) REFERENCES VENT_AARSAK;
