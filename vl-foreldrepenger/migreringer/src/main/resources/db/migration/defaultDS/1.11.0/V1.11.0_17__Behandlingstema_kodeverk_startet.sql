insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'ENGST_FODS', 'ab0050', 'Engangsstønad ved fødsel', null, to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'BEHANDLING_TEMA', 'ENGST_ADOP', 'ab0027', 'Engangsstønad ved adopsjon', null, to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
values(seq_kodeliste.nextval, 'BEHANDLING_TEMA', '-', null, 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'));

