insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
values ('fordeling.venter.intervall', 'Fordeling venter - intervall', 'INGEN', 'PERIOD', 'Fordeling venter i angitt intervall');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
values (SEQ_KONFIG_VERDI.nextval, 'fordeling.venter.intervall', 'INGEN', 'P1D', to_date('01.01.2016', 'dd.mm.yyyy'));
