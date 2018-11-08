-- DOKUMENT_MAL_TYPE nye rader
INSERT INTO DOKUMENT_MAL_TYPE (kode, navn, generisk, DOKSYS_KODE) VALUES ('INNTID', 'Ikke mottat søknad', 'N', '000091');

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('søk.antall.uker', 'Søk antall uker', 'INGEN', 'PERIOD', 'Antall uker før uttak en bruker må søke.');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'søk.antall.uker', 'INGEN', 'P4W', to_date('01.01.2016', 'dd.mm.yyyy'));
