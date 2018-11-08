-- Ny VedtakResultatType for Innsyn
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'DELVIS_INNVILGET', 'delvis innvilget', 'delvis innvilget', 'VEDTAK_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
