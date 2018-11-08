INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('DOKUMENT_KATEGORI', 'Dokumentkategorier', 'NAV Dokumentkategorier', 'Kodeverkforvaltning', 'http://nav.no/kodeverk/Kodeverk/Dokumentkategorier', '1', 'Dokumentkategorier', 'J', 'J');

insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'KLGA', 'KA', 'Klage eller anke', 'Klage eller anke', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'ITSKJ', 'IS', 'Ikke tolkbart skjema', 'Ikke tolkbart skjema', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'SOKN', 'SOK', 'Søknad', 'Søknad', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'ESKJ', 'ES', 'Elektronisk skjema', 'Elektronisk skjema', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'BRV', 'B', 'Brev', 'Brev', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'EDIALOG', 'ELEKTRONISK_DIALOG', 'Elektronisk dialog', 'Elektronisk dialog', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'FNOT', 'FORVALTNINGSNOTAT', 'Forvaltningsnotat', 'Forvaltningsnotat', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'IBRV', 'IB', 'Informasjonsbrev', 'Informasjonsbrev', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'KONVEARK', 'KD', 'Konvertert fra elektronisk arkiv', 'Konvertert fra elektronisk arkiv', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'KONVSYS', 'KS', 'Konverterte data fra system', 'Konverterte data fra system', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'PUBEOS', 'PUBL_BLANKETT_EOS', 'Publikumsblankett EØS', 'Publikumsblankett EØS', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'SEDOK', 'SED', 'Strukturert elektronisk dokument - EU/EØS', 'Strukturert elektronisk dokument - EU/EØS', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'TSKJ', 'TS', 'Tolkbart skjema', 'Tolkbart skjema', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'VBRV', 'VB', 'Vedtaksbrev', 'Vedtaksbrev', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'DOKUMENT_KATEGORI', to_date('2000-01-01', 'YYYY-MM-DD'));
