update KODELISTE set ekstra_data = '{"mal": "TYPE10"}'
where kodeverk = 'HISTORIKKINNSLAG_TYPE' and KODE = 'OVST_UTTAK';

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'UTTAK_PERIODE_FOM', 'Fradato uttaksperiode', 'Fradato uttaksperiode', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_OPPLYSNING_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'UTTAK_PERIODE_TOM', 'Tildato uttaksperiode', 'Tildato uttaksperiode', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_OPPLYSNING_TYPE');
