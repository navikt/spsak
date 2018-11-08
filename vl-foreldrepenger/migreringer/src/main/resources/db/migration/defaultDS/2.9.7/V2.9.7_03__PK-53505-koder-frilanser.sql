-- fikser
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom, ekstra_data)
VALUES (seq_kodeliste.nextval, 'ARBEID_TYPE', 'FRILANSER', null, 'Frilanser, samlet aktivitet', NULL, to_date('2000-01-01', 'YYYY-MM-DD'), '{ "gui": "true" }');

update KODELISTE_RELASJON set kode1 = 'FRILANSER' where kodeverk1 = 'ARBEID_TYPE' and kode1 = 'FRILANSER_OPPDRAGSTAKER' and kodeverk2 = 'OPPTJENING_AKTIVITET_TYPE';
update KODELISTE_RELASJON set kode2 = 'FRILANSER' where kodeverk2 = 'ARBEID_TYPE' and kode2 = 'FRILANSER_OPPDRAGSTAKER' and kodeverk1 = 'OPPTJENING_AKTIVITET_TYPE';
