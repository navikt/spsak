update KODELISTE_NAVN_I18N set navn = 'Engangsstønad' where KL_KODE in (
  select kode from kodeliste where kode = 'ES' and kodeverk = 'FAGSAK_YTELSE'
);
