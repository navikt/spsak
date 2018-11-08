insert into kodeliste (id, kode, kodeverk, gyldig_fom) values (seq_kodeliste.nextval, 'DATE', 'KONFIG_VERDI_TYPE', to_date('2000-01-01', 'yyyy-mm-dd'));

insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('dato.for.nye.beregningsregler', 'Dato for nye beregningsregler', 'INGEN', 'DATE', 'Dato for nye beregningsregler');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom) values (SEQ_KONFIG_VERDI.nextval, 'dato.for.nye.beregningsregler', 'INGEN', '2010-01-01', to_date('01.01.2017', 'dd.mm.yyyy'));
