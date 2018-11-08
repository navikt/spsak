ALTER TABLE BEHANDLING
  ADD behandlende_enhet_navn varchar2(320) null;

COMMENT ON COLUMN BEHANDLING.behandlende_enhet_navn IS 'Navn på behandlende enhet';

ALTER TABLE BEHANDLING
  ADD behandlende_enhet_arsak varchar2(800) null;

COMMENT ON COLUMN BEHANDLING.behandlende_enhet_arsak IS 'Fritekst for hvorfor behandlende enhet har blitt endret';

ALTER TABLE BEHANDLING
 ADD behandlingstid_frist date NOT NULL;

COMMENT ON COLUMN BEHANDLING.behandlingstid_frist IS 'Frist for når behandlingen skal være ferdig';

-- Ny behandlingType Søknad
INSERT INTO BEHANDLING_TYPE (kode, navn) VALUES ('BT-005', 'Søknad');
update BEHANDLING_TYPE set NAV_OFFISIELL_KODE = 'ae0034' where KODE = 'BT-005';

-- Ny HistorikkinnslagType
INSERT INTO HISTORIKKINNSLAG_TYPE(kode, navn) VALUES ('BYTT_ENHET', 'Byttet behandlende enhet');

