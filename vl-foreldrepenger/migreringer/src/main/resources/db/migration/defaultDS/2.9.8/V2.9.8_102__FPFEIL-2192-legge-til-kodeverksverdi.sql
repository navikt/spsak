-- Dette er kodeverksverdi som betyr "ingen verdi" hos infotrrygd.
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, beskrivelse, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'RELATERT_YTELSE_STATUS', 'xx', 'xx', 'Ingen verdi', to_date('2000-01-01', 'YYYY-MM-DD'));
