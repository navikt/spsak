INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('VARIANT_FORMAT', 'Variantformater', 'NAV Variantformater', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Variantformater', '1', 'Variantformater', 'J', 'J');

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'VARIANT_FORMAT', 'PROD', 'PRODUKSJON', 'Produksjonsformat', 'Produksjonsformat', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'VARIANT_FORMAT', 'ARKIV', 'ARKIV', 'Arkivformat', 'Arkivformat', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'VARIANT_FORMAT', 'SKANM', 'SKANNING_META', 'Skanning metadata', 'Skanning metadata', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'VARIANT_FORMAT', 'BREVB', 'BREVBESTILLING', 'Brevbestilling data', 'Brevbestilling data', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'VARIANT_FORMAT', 'ORIG', 'ORIGINAL', 'Originalformat', 'Originalformat', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'VARIANT_FORMAT', 'FULL', 'FULLVERSJON', 'Versjon med infotekster', 'Versjon med infotekster', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'VARIANT_FORMAT', 'SLADD', 'SLADDET', 'Sladdet format', 'Sladdet format', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'VARIANT_FORMAT', 'PRDLF', 'PRODUKSJON_DLF', 'Produksjonsformat DLF', 'Produksjonsformat DLF', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'VARIANT_FORMAT', '-', null, 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'));
