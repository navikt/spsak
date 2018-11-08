insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
values ('uttak.tidligst.før.fødsel', 'Tidligste uttak før fødsel', 'INGEN', 'PERIOD', 'Tidligste lovlige oppstart av uttak av foreldrepenger før fødsel.');

insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
values (SEQ_KONFIG_VERDI.nextval, 'uttak.tidligst.før.fødsel', 'INGEN', 'P12W', to_date('01.01.2016', 'dd.mm.yyyy'));
