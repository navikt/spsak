ALTER TABLE BG_PR_STATUS_OG_ANDEL ADD lagt_til_av_saksbehandler VARCHAR2(1 CHAR) DEFAULT 'N';

ALTER TABLE BG_PR_STATUS_OG_ANDEL ADD CONSTRAINT CHK_BG_PR_STATUS_OG_ANDEL_01 CHECK (lagt_til_av_saksbehandler IS NULL OR lagt_til_av_saksbehandler IN ('J', 'N'));

COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.lagt_til_av_saksbehandler IS 'Angir om andel er lagt til av saksbehandler manuelt';
