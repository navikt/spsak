DELETE FROM KODELISTE
where KODEVERK = 'SKJERMLENKE_TYPE' and KODE = 'BEHANDLE_KLAGE';

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'KLAGE_BEH_NFP', 'Behandle klage NFP', 'Behandle klage NFP', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'KLAGE_BEH_NK', 'Behandle klage NK', 'Behandle klage NK', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
