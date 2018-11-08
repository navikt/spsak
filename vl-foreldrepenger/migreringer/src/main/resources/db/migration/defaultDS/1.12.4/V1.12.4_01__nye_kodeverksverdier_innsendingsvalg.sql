insert into KODELISTE (id, kode, offisiell_kode,  navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'IKKE_VALGT' , null, 'Ikke valgt',null, 'INNSENDINGSVALG', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kode, offisiell_kode,  navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'VEDLEGG_SENDES_AV_ANDRE' , null, 'Vedlegg sendes av andre',null, 'INNSENDINGSVALG', to_date('2000-01-01', 'YYYY-MM-DD'));

insert into KODELISTE (id, kode, offisiell_kode,  navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'VEDLEGG_ALLEREDE_SENDT' , null, 'Vedlegg aallerede sendt', null, 'INNSENDINGSVALG', to_date('2000-01-01', 'YYYY-MM-DD'));
