ALTER TABLE AKTIVITETS_AVTALE ADD BESKRIVELSE VARCHAR2(400);

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'MILITÆR_ELLER_SIVILTJENESTE', null, 'Militær eller siviltjeneste', NULL, to_date('2000-01-01', 'YYYY-MM-DD'));
