delete from KODELISTE_NAVN_I18N where KL_KODEVERK = 'ARBEIDSKATEGORI' and kl_kode ='ANNET';
alter table IAY_YTELSE_GRUNNLAG disable constraint FK_IAY_YTELSE_GRUNNLAG_81;
update KODELISTE set KODE='UGYLDIG', BESKRIVELSE = 'ugyldig' where KODEVERK = 'ARBEIDSKATEGORI' and KODE = 'ANNET';
update IAY_YTELSE_GRUNNLAG set ARBEIDSKATEGORI='UGYLDIG' where ARBEIDSKATEGORI = 'ANNET' and KL_ARBEIDSKATEGORI='ARBEIDSKATEGORI';
alter table IAY_YTELSE_GRUNNLAG ENABLE constraint FK_IAY_YTELSE_GRUNNLAG_81;
insert into KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn) values (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'ARBEIDSKATEGORI',	'UGYLDIG',	'NB',	'ugyldig');
