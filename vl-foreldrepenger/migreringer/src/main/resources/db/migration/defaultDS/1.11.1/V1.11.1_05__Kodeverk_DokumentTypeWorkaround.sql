INSERT INTO kodeverk (kode, navn, beskrivelse)
VALUES ('DOKUMENT_TYPE_WORKAROUND', 'Dokument type workaournd', 'PKMANTIS-533 Workaround for dokument type som VL bruker selv om de ikke finnes i offisielt kodeverk');

insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'WA_INNHEN', '000049', 'Innhent dokumentasjon', 'Innhent dokumentasjon', 'DOKUMENT_TYPE_WORKAROUND', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'WA_POSVED', '000048', 'Positivt vedtaksbrev', 'Positivt vedtaksbrev', 'DOKUMENT_TYPE_WORKAROUND', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'WA_HENLEG', '000050', 'Behandling avbrutt', 'Behandling avbrutt', 'DOKUMENT_TYPE_WORKAROUND', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'WA_AVSLAG', '000051', 'Avslagsbrev', 'Avslagsbrev', 'DOKUMENT_TYPE_WORKAROUND', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'WA_UENDRE', '000052', 'Uendret utfall', 'Uendret utfall', 'DOKUMENT_TYPE_WORKAROUND', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'WA_REVURD', '000058', 'Revurdering', 'Revurdering', 'DOKUMENT_TYPE_WORKAROUND', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'WA_FORLEN', '000056', 'Forlenget saksbehandlingstid', 'Forlenget saksbehandlingstid', 'DOKUMENT_TYPE_WORKAROUND', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'WA_KLAGAV', '000054', 'Vedtak om avvist klage', 'Vedtak om avvist klage', 'DOKUMENT_TYPE_WORKAROUND', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'WA_KLAGVE', '000055', 'Vedtak om stadfestelse', 'Vedtak om stadfestelse', 'DOKUMENT_TYPE_WORKAROUND', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'WA_KLAGNY', '000059', 'Vedtak opphevet, sendt til ny behandling', 'Vedtak opphevet, sendt til ny behandling', 'DOKUMENT_TYPE_WORKAROUND', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, offisiell_kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'WA_KLAGOV', '000060', 'Overføring til NAV Klageinstans', 'Overføring til NAV Klageinstans', 'DOKUMENT_TYPE_WORKAROUND', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, '-', 'Ikke definert', 'Ikke definert', 'DOKUMENT_TYPE_WORKAROUND', to_date('2000-01-01', 'YYYY-MM-DD'));
