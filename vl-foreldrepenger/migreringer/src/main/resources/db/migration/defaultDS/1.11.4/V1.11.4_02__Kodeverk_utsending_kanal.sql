INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('UTSENDING_KANAL', 'Utsendingskanaler', 'NAV Utsendingskanaler', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Utsendingskanaler', '1', 'Utsendingskanaler', 'J', 'J');

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'UTSENDING_KANAL', 'PSELV', 'PSELV', 'PSELV', 'PSELV', to_date('2006-07-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'UTSENDING_KANAL', 'EESSI', 'EESSI', 'EESSI', 'EESSI', to_date('2006-07-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'UTSENDING_KANAL', 'ALTINN', 'ALTINN', 'ALTINN', 'ALTINN', to_date('2006-07-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'UTSENDING_KANAL', 'NAV_NO', 'NAV_NO', 'Ditt NAV', 'Ditt NAV', to_date('2006-07-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'UTSENDING_KANAL', 'EPOST', 'E_POST', 'E-post', 'E-post', to_date('2006-07-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'UTSENDING_KANAL', 'SENT_PRT', 'S', 'Sentral print', 'Sentral print', to_date('2006-07-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'UTSENDING_KANAL', 'LOK_PRT', 'L', 'Lokal print', 'Lokal print', to_date('2006-07-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'UTSENDING_KANAL', 'S_DIG_POST', 'SDP', 'Sikker digital post', 'Sikker digital post', to_date('2006-07-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'UTSENDING_KANAL', 'EIA', 'EIA', 'EIA', 'EIA', to_date('2006-07-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'UTSENDING_KANAL', '-', null, 'Ikke definert', 'Ikke definert', to_date('2006-07-01', 'YYYY-MM-DD'));
