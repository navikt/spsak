alter table AKSJONSPUNKT_DEF drop column AKSJONSPUNKT_KATEGORI cascade constraints;
   
delete from KODELISTE where kodeverk = 'AKSJONSPUNKT_KATEGORI';