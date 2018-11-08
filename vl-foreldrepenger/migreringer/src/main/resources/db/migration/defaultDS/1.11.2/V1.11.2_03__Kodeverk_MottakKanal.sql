INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('MOTTAK_KANAL', 'Mottakskanaler', 'NAV Mottakskanaler', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Mottakskanaler', '1', 'Mottakskanaler', 'J', 'J');

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'MOTTAK_KANAL', 'ALTINN', 'ALTINN', 'Altinn', 'Altinn', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'MOTTAK_KANAL', 'EIA', 'EIA', 'EIA', 'EIA', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'MOTTAK_KANAL', 'EKST_OPPS', 'EKST_OPPS', 'Eksternt oppslag', 'Eksternt oppslag', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'MOTTAK_KANAL', 'NAV_NO', 'NAV_NO', 'Ditt NAV', 'Ditt NAV', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'MOTTAK_KANAL', 'SKAN_NETS', 'SKAN_NETS', 'Skanning Nets', 'Skanning Nets', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'MOTTAK_KANAL', 'SKAN_PEN', 'SKAN_PEN', 'Skanning Pensjon', 'Skanning Pensjon', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'MOTTAK_KANAL', '-', null, 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'));
