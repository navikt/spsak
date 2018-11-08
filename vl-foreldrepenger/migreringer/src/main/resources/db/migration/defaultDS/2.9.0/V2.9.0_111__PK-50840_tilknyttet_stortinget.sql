CREATE TABLE ORG_MANUELL_BEHANDLING (
  Id                           NUMBER(19)        NOT NULL,
  Virksomhetsnummer            VARCHAR2(200 CHAR) NOT NULL,
  Manuell_beh_arsak            VARCHAR2(1000 CHAR) NOT NULL,
    opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
    opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    endret_av       VARCHAR2(20 CHAR),
    endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_ORG_MANUELL_BEHANDLING PRIMARY KEY (Id)
);

COMMENT ON TABLE ORG_MANUELL_BEHANDLING
IS 'Organisasjonsnummer som vil trigge aksjonspunkt i avklar fakta om uttak';
COMMENT ON COLUMN ORG_MANUELL_BEHANDLING.Virksomhetsnummer
IS 'Organisasjonsnummer';
COMMENT ON COLUMN ORG_MANUELL_BEHANDLING.Manuell_beh_arsak
IS 'Kort beskrivelse';

CREATE SEQUENCE SEQ_ORG_MANUELL_BEHANDLING MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

INSERT INTO ORG_MANUELL_BEHANDLING (id, Virksomhetsnummer, Manuell_beh_arsak) VALUES (SEQ_ORG_MANUELL_BEHANDLING.nextval, '971524960', 'Stortingsrepresentant');

INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, AKSJONSPUNKT_TYPE, SKJERMLENKE_TYPE)
VALUES ('5072', 'Søker er stortingsrepresentant/administrativt ansatt i Stortinget', 'VURDER_UTTAK.UT', 'Fastsetter uttaksperioder manuelt.', '-', 'J', 'MANU', 'UTTAK');
