
-- Endre kolonne
ALTER TABLE BG_PR_STATUS_OG_ANDEL RENAME COLUMN nyoppstartet_frilanser TO fastsatt_av_saksbehandler;
COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.fastsatt_av_saksbehandler IS 'Oppgir om månedsinntekten er fastsatt av saksbehandler ved faktaavklaring';
