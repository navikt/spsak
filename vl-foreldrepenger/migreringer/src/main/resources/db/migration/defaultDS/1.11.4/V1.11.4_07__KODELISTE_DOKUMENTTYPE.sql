merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE' and k.offisiell_KODE = 'I000046')
when not matched then
insert (id, kode, offisiell_kode,  navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'I000046', 'I000046', 'Kvittering dokumentinnsending', 'Kvittering dokumentinnsending', 'DOKUMENT_TYPE', to_date('2017-04-25', 'YYYY-MM-DD'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'DOKUMENT_TYPE' and k.offisiell_KODE = 'I000062')
when not matched then
insert (id, kode, offisiell_kode,  navn, beskrivelse, kodeverk, gyldig_fom) values
  (seq_kodeliste.nextval, 'I000062', 'I000062', 'Bekreftelse på ventet fødselsdato', 'Bekreftelse på ventet fødselsdato', 'DOKUMENT_TYPE', to_date('2017-04-25', 'YYYY-MM-DD'));
