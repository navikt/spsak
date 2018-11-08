ALTER TABLE BG_PR_STATUS_OG_ANDEL
ADD lønnsendring_i_perioden CHAR(1) check (lønnsendring_i_perioden IN ('J', 'N'));

COMMENT ON COLUMN BG_PR_STATUS_OG_ANDEL.lønnsendring_i_perioden IS 'true hvis inntektsmelding mangler og saksbehandler har vurdert at bruker har hatt lønnsendring i beregningsperioden';

--Nye Aksjonspunkter
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'FAKTA_OM_BEREGNING_TILFELLE', 'VURDER_LØNNSENDRING', 'Vurder lønnsendring', 'Vurder lønnsendring', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));
INSERT INTO KODELISTE (ID, KODEVERK, KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL, 'FAKTA_OM_BEREGNING_TILFELLE', 'FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING', 'Fastsett månedslønn arbeidstaker uten inntektsmelding', 'Saksbehandler har vurdert at bruker har hatt lønnsendring i den ordinære beregningsperioden', 'NB', to_date('01.01.2000', 'dd.mm.yyyy'), to_date('31.12.9999', 'dd.mm.yyyy'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'LØNNSENDRING_I_PERIODEN', 'Lønnsendring i beregningsperioden', 'Lønnsendring i beregningsperioden', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');
