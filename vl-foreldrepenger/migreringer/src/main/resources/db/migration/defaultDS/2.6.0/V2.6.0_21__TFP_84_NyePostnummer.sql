UPDATE KODEVERK SET KODEVERK_EIER_VER='7' WHERE KODE='POSTSTED';
UPDATE KODEVERK SET KODEVERK_EIER_VER='4' WHERE KODE='DOKUMENT_TYPE_ID';
UPDATE KODEVERK SET KODEVERK_SYNK_NYE = 'J', KODEVERK_SYNK_EKSISTERENDE='J' WHERE KODE in ('POSTSTED', 'LANDKODER', 'DOKUMENT_TYPE_ID');

UPDATE KODELISTE SET NAVN='ATRÅ' WHERE KODEVERK='POSTSTED' AND KODE='3666';
UPDATE KODELISTE SET NAVN='STORMOLLA' WHERE KODEVERK='POSTSTED' AND KODE='8328';

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'POSTSTED' and k.KODE = '0140') when not matched then
  insert (id, kodeverk, kode, offisiell_kode,  navn, beskrivelse, gyldig_fom)
  values (seq_kodeliste.nextval, 'POSTSTED', '0140', '0140', 'OSLO', '', to_date('2017-01-01', 'YYYY-MM-DD'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'POSTSTED' and k.KODE = '1108') when not matched then
  insert (id, kodeverk, kode, offisiell_kode,  navn, beskrivelse, gyldig_fom)
  values (seq_kodeliste.nextval, 'POSTSTED', '1108', '1108', 'OSLO', '', to_date('2017-10-01', 'YYYY-MM-DD'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'POSTSTED' and k.KODE = '2413') when not matched then
  insert (id, kodeverk, kode, offisiell_kode,  navn, beskrivelse, gyldig_fom)
  values (seq_kodeliste.nextval, 'POSTSTED', '2413', '2413', 'ELVERUM', '', to_date('2017-10-01', 'YYYY-MM-DD'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'POSTSTED' and k.KODE = '2414') when not matched then
  insert (id, kodeverk, kode, offisiell_kode,  navn, beskrivelse, gyldig_fom)
  values (seq_kodeliste.nextval, 'POSTSTED', '2414', '2414', 'ELVERUM', '', to_date('2017-10-01', 'YYYY-MM-DD'));

merge into KODELISTE k using dual on (dual.dummy is not null and k.kodeverk = 'POSTSTED' and k.KODE = '3129') when not matched then
  insert (id, kodeverk, kode, offisiell_kode,  navn, beskrivelse, gyldig_fom)
  values (seq_kodeliste.nextval, 'POSTSTED', '3129', '3129', 'TØNSBERG', '', to_date('2017-10-01', 'YYYY-MM-DD'));
