INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES ('BEREGNINGSGRUNNLAG_TILSTAND', 'Kodeverk for tilstand til beregningsgrunnlag', 'Kodeverk for tilstand til beregningsgrunnlag','VL');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'OPPRETTET', 'Opprettet', 'Opprettet', to_date('2000-01-01', 'YYYY-MM-DD'), 'BEREGNINGSGRUNNLAG_TILSTAND');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'FORESLÅTT', 'Foreslått', 'Foreslått', to_date('2000-01-01', 'YYYY-MM-DD'), 'BEREGNINGSGRUNNLAG_TILSTAND');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'FASTSATT', 'Fastsatt', 'Fastsatt', to_date('2000-01-01', 'YYYY-MM-DD'), 'BEREGNINGSGRUNNLAG_TILSTAND');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'KOFAKBER_UT', 'Kontroller fakta beregningsgrunnlag - Ut', 'Kontroller fakta beregningsgrunnlag - Ut', to_date('2000-01-01', 'YYYY-MM-DD'), 'BEREGNINGSGRUNNLAG_TILSTAND');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'FASTSATT_INN', 'Fastsatt - Inn', 'Fastsatt - Inn', to_date('2000-01-01', 'YYYY-MM-DD'), 'BEREGNINGSGRUNNLAG_TILSTAND');
