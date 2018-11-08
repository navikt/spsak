-- Nytt kodeverk for perioder for utesttelse
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('UTSETTELSE_GRADERING_KVOTE', 'N', 'N', 'Utsettelse-/Graderingsperiode', 'Kodeverk for utsettelse-/graderingsperiode');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'FEDREKVOTE', 'Fedrekvote', 'Fedrekvote', 'UTSETTELSE_GRADERING_KVOTE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'MODREKVOTE', 'Mødrekvote', 'Mødrekvote', 'UTSETTELSE_GRADERING_KVOTE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'FELLESPERIODE', 'Fellesperiode', 'Fellesperiode', 'UTSETTELSE_GRADERING_KVOTE', to_date('2000-01-01', 'YYYY-MM-DD'));


-- Nytt kodeverk for perioder for utesttelse
insert into KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse) values ('UTSETTELSE_AARSAK', 'N', 'N', 'Utsettelseårsak', 'Kodeverk for årsak til utsettelse');

insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'ARBEID', 'Arbeid', 'Arbeid', 'UTSETTELSE_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'FERIE', 'Lovbestemt ferie', 'Lovbestemt ferie', 'UTSETTELSE_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'AVH_AV_HJLP_GR_SKDM', 'Avhengig av hjelp grunnet sykdom', 'Avhengig av hjelp grunnet sykdom', 'UTSETTELSE_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'SOKR_ER_INNLGT', 'Søker er innlagt i helseinstitusjon', 'Søker er innlagt i helseinstitusjon', 'UTSETTELSE_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'BARN_ER_INNLGT', 'Barn er innlagt i helseinstitusjon', 'Barn er innlagt i helseinstitusjon', 'UTSETTELSE_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
