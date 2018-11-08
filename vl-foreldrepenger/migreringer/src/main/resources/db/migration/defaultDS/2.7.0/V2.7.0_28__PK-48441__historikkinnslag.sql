-- HISTORIKK_ENDRET_FELT_TYPE TIDSBEGRENSET_ARBEIDSFORHOLD
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'ENDRING_TIDSBEGRENSET_ARBEIDSFORHOLD', 'Endring tidsbegrenset arbeidsforhold', 'Endring tidsbegrenset arbeidsforhold', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');

-- HISTORIKK_ENDRET_FELT_VERDI_TYPE
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'TIDSBEGRENSET_ARBEIDSFORHOLD', 'Endre til tidsbegrenset arbeidsforhold', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'IKKE_TIDSBEGRENSET_ARBEIDSFORHOLD', 'Endre til ikke tidsbegrenset arbeidsforhold', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
