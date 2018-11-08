INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5016', 'Vurder søknad om overføring av kvote', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES  (seq_kodeliste.nextval, 'MANUELL_BEHANDLING_AARSAK', '5016','NB', 'Vurder søknad om overføring av kvote');
