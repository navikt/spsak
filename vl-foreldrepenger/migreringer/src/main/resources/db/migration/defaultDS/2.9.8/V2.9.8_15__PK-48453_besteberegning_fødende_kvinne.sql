ALTER TABLE BG_PR_STATUS_OG_ANDEL ADD besteberegning_pr_aar  NUMBER(19,2);

COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.besteberegning_pr_aar IS 'Inntekt fastsatt av saksbehandler ved besteberegning for fødende kvinne';

INSERT INTO KODELISTE (ID, KODEVERK, KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'FAKTA_OM_BEREGNING_TILFELLE', 'FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE', 'Fastsett besteberegning fødende kvinne', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, gyldig_tom, kodeverk)
VALUES (seq_kodeliste.nextval, 'DAGPENGER_INNTEKT', 'Dagpenger', to_date('2000-01-01', 'YYYY-MM-DD'), to_date('31.12.9999', 'dd.mm.yyyy'), 'HISTORIKK_ENDRET_FELT_TYPE');
