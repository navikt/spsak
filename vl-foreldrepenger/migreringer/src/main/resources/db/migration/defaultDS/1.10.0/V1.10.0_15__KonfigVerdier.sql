insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('saksbehandling.frist.uker', 'Saksbehandlingsfrist', 'INGEN', 'INTEGER', 'Frist for når saksbehandlingen skal være ferdigbehandlet etter at den er mottatt');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'saksbehandling.frist.uker', 'INGEN', '6', to_date('01.01.2016', 'dd.mm.yyyy'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('brev.svarfrist.dager', 'Brukers svarfrist', 'INGEN', 'PERIOD', 'Brukers svartfrist (periode)');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'brev.svarfrist.dager', 'INGEN', 'P3W', to_date('01.01.2016', 'dd.mm.yyyy'));
