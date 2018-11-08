insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
values ('sak.frist.innsending.dok.uker', 'Frist for innsending av dok', 'INGEN', 'INTEGER', ' Frist i uker fom siste vedtaksdato (positivt heltall)');

insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
values (SEQ_KONFIG_VERDI.nextval, 'sak.frist.innsending.dok.uker', 'INGEN', '6', to_date('15.10.2018', 'dd.mm.yyyy'));
