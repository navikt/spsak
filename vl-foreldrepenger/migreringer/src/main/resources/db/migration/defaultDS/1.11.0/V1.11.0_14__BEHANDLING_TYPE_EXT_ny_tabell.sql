-- BEHANDLING_TYPE
CREATE TABLE BEHANDLING_TYPE (
  kode VARCHAR2(100 char) NOT NULL,
  kl_kode VARCHAR2(100 char) AS ('BEHANDLING_TYPE') NOT NULL,
  behandlingstid_frist_uker INT NOT NULL,
  behandlingstid_varselbrev CHAR(1) DEFAULT 'N' NOT NULL CHECK (behandlingstid_varselbrev IN ('J', 'N')),
  navn varchar2(50 char) AS ('Hentes fra KODELISTE.navn') NOT NULL,
  beskrivelse varchar2(4000 char) AS ('Hentes fra KODELISTE.beskrivelse') NOT NULL,
  opprettet_av    VARCHAR2(20 char) DEFAULT 'VL' NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 char),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_BEHANDLING_TYPE PRIMARY KEY (KODE)
);

COMMENT ON TABLE BEHANDLING_TYPE is 'Utvidelse av kodeverk for behandlingstyper';
COMMENT ON COLUMN BEHANDLING_TYPE.kode is 'Kodeverk Primary Key';
COMMENT ON COLUMN BEHANDLING_TYPE.kl_kode is 'Referanse til KODELISTE.KODEVERK';
COMMENT ON COLUMN BEHANDLING_TYPE.behandlingstid_frist_uker is 'Maksimal behandlingstid i antall uker';
COMMENT ON COLUMN BEHANDLING_TYPE.behandlingstid_varselbrev is 'Angir om det skal sendes varselbrev til bruker når behandlingstidfristen har utgått';
COMMENT ON COLUMN BEHANDLING_TYPE.navn is 'Kommentar om at denne verdien hentes fra KODELISTE';
COMMENT ON COLUMN BEHANDLING_TYPE.beskrivelse is 'Kommentar om at denne verdien hentes fra KODELISTE';

-- Førstegangssøknad
INSERT INTO BEHANDLING_TYPE (kode, behandlingstid_frist_uker, behandlingstid_varselbrev)
SELECT kode, 6 as behandlingstid_frist_uker, 'J' as behandlingstid_varselbrev
FROM KODELISTE
WHERE kodeverk = 'BEHANDLING_TYPE' and kode = 'BT-002';

-- Klage
INSERT INTO BEHANDLING_TYPE (kode, behandlingstid_frist_uker, behandlingstid_varselbrev)
SELECT kode, 12 as behandlingstid_frist_uker, 'N' as behandlingstid_varselbrev
FROM KODELISTE
WHERE kodeverk = 'BEHANDLING_TYPE' and kode = 'BT-003';

-- Revurdering
INSERT INTO BEHANDLING_TYPE (kode, behandlingstid_frist_uker, behandlingstid_varselbrev)
SELECT kode, 6 as behandlingstid_frist_uker, 'N' as behandlingstid_varselbrev
FROM KODELISTE
WHERE kodeverk = 'BEHANDLING_TYPE' and kode = 'BT-004';

-- Søknad
INSERT INTO BEHANDLING_TYPE (kode, behandlingstid_frist_uker, behandlingstid_varselbrev)
SELECT kode, 6 as behandlingstid_frist_uker, 'N' as behandlingstid_varselbrev
FROM KODELISTE
WHERE kodeverk = 'BEHANDLING_TYPE' and kode = 'BT-005';

-- Konfigurasjonsparametre for maksimal behandlingstid fjernes
DELETE FROM KONFIG_VERDI WHERE konfig_kode in ('saksbehandling.frist.uker', 'saksbehandling.frist.uker.klagebehandling');
DELETE FROM KONFIG_VERDI_KODE WHERE kode in ('saksbehandling.frist.uker', 'saksbehandling.frist.uker.klagebehandling');
