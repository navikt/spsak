-- Trenger skille mellom kodeverk av type sammensatt og enkel kodeliste
ALTER TABLE KODEVERK ADD (sammensatt VARCHAR2(1 CHAR) DEFAULT 'N');
UPDATE KODEVERK SET sammensatt = 'N';

-- Legger til kodeverk som hører innunder sammensatt kodeverk Geografi/Geopolitisk

INSERT INTO KODEVERK (kode, kodeverk_eier, kodeverk_eier_navn, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse, sammensatt)
VALUES ('LANDKODE_ISO2', 'Kodeverkforvaltning', 'LandkoderISO2', 'http://nav.no/kodeverk/Kodeverk/LandkoderISO2', 2, 'J', 'J', 'Landkode ISO2', 'Landkode 2 bokstav', 'N');

INSERT INTO KODEVERK (kode, kodeverk_eier, kodeverk_eier_navn, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse, sammensatt)
VALUES ('GEOPOLITISK', 'Kodeverkforvaltning', 'Geopolitisk', 'http://nav.no/kodeverk/Kodeverk/Geopolitisk', 1, 'J', 'J', 'Geopolitisk', 'Geopolitiske områder', 'N');

INSERT INTO KODEVERK (kode, kodeverk_eier, kodeverk_eier_navn, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse, sammensatt)
VALUES ('LANDGRUPPER', 'Kodeverkforvaltning', 'Landgrupper', 'http://nav.no/kodeverk/Kodeverk/Landgrupper', 2, 'J', 'J', 'Landgruppe', 'Landgruppe', 'N');

INSERT INTO KODEVERK (kode, kodeverk_eier, kodeverk_eier_navn, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse, sammensatt)
VALUES ('BYDELER', 'Kodeverkforvaltning', 'Bydeler', 'http://nav.no/kodeverk/Kodeverk/Bydeler', 4, 'J', 'J', 'Bydel', 'Bydel', 'N');

INSERT INTO KODEVERK (kode, kodeverk_eier, kodeverk_eier_navn, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse, sammensatt)
VALUES ('FYLKER', 'Kodeverkforvaltning', 'Fylker', 'http://nav.no/kodeverk/Kodeverk/Fylker', 5, 'J', 'J', 'Fylke', 'Fylke', 'N');

INSERT INTO KODEVERK (kode, kodeverk_eier, kodeverk_eier_navn, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse, sammensatt)
VALUES ('GEOGRAFI', 'Kodeverkforvaltning', 'Geografi', 'http://nav.no/kodeverk/Kodeverk/Geografi', 4, 'J', 'J', 'Geografi', 'Geografi', 'J');

INSERT INTO KODEVERK (kode, kodeverk_eier, kodeverk_eier_navn, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse, sammensatt)
VALUES ('GEOPOLITISKE_REGIONER', 'Kodeverkforvaltning', 'Geopolitiske regioner', 'http://nav.no/kodeverk/Kodeverk/Geopolitiske_20regioner', 1, 'J', 'J', 'Geopolitiske regioner', 'Geopolitiske regioner', 'J');


-- Ny tabell for relasjon mellom kodeliste elementer
CREATE TABLE KODELISTE_RELASJON (
  id number(19, 0) not null,
  kodeverk1 VARCHAR(100 char) NOT NULL,
  kode1 VARCHAR2(100 char) NOT NULL,
  kodeverk2 VARCHAR(100 char) NOT NULL,
  kode2 VARCHAR2(100 char) NOT NULL,
  gyldig_fom DATE DEFAULT SYSDATE NOT NULL,
  gyldig_tom DATE DEFAULT TO_DATE('31.12.9999','DD.MM.YYYY') NOT NULL,
  opprettet_av    VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 char),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_KODELISTE_RELASJON PRIMARY KEY (id)
);

CREATE SEQUENCE seq_kodeliste_relasjon MINVALUE 1 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;
ALTER TABLE kodeliste_relasjon ADD CONSTRAINT fk_kodeliste_relasjon_1 FOREIGN KEY (kodeverk1, kode1) REFERENCES KODELISTE(kodeverk, kode);
ALTER TABLE kodeliste_relasjon ADD CONSTRAINT fk_kodeliste_relasjon_2 FOREIGN KEY (kodeverk2, kode2) REFERENCES KODELISTE(kodeverk, kode);
CREATE INDEX IDX_KODELISTE_RELASJON_1 ON KODELISTE_RELASJON(kodeverk1, kode1);
CREATE INDEX IDX_KODELISTE_RELASJON_2 ON KODELISTE_RELASJON(kodeverk2, kode2);

COMMENT ON TABLE KODELISTE_RELASJON IS 'Relasjon mellom kodeliste elementer: kode1 og kode2';
COMMENT ON COLUMN KODELISTE_RELASJON.kodeverk1 IS 'Kodeverk for kode 1';
COMMENT ON COLUMN KODELISTE_RELASJON.kode1 IS 'Kode 1';
COMMENT ON COLUMN KODELISTE_RELASJON.kodeverk2 IS 'Kodeverk for kode 2';
COMMENT ON COLUMN KODELISTE_RELASJON.kode2 IS 'Kode 2';
COMMENT ON COLUMN KODELISTE_RELASJON.gyldig_fom IS 'Gyldig fra og med dato';
COMMENT ON COLUMN KODELISTE_RELASJON.gyldig_tom IS 'Gyldig til og med dato';
