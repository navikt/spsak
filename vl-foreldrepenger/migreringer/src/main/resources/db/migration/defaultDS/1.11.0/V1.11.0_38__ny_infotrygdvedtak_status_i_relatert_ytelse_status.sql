insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) values
  (seq_kodeliste.nextval, 'RELATERT_YTELSE_STATUS', 'L', 'L', 'Løpende', 'Løpende', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom) values
  (seq_kodeliste.nextval, 'RELATERT_YTELSE_STATUS', 'A', 'A', 'Avsluttet', 'Avsluttet',  to_date('2000-01-01', 'YYYY-MM-DD'));

