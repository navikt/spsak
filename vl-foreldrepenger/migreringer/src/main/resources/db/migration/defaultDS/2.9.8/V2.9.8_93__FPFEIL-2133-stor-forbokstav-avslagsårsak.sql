update KODELISTE_NAVN_I18N set navn = upper(substr(navn,1,1)) || substr(navn,2,9999) where KL_KODE in (
  select kode from kodeliste where kodeverk = 'AVSLAGSARSAK'
);
