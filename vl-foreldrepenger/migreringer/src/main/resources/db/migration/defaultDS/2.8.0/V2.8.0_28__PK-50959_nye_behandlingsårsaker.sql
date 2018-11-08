insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'RE-REGISTEROPPLYSNING', 'Registeropplysning', 'Revurdering pga. registeropplysninger', 'BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'RE-YTELSE', 'Ytelse', 'Opplysninger om ytelse', 'BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
