INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'MAKSGRENSE_OVERSREDET', 'Periode er etter maksgrense for utbetaling av foreldrepenger.',
   'Periode er etter maksgrense for utbetaling av foreldrepenger.', to_date('2000-01-01', 'YYYY-MM-DD'), 'AVKORTING_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES
  (seq_kodeliste.nextval, 'IKKE_OMSORG', 'Søker har ikke omsorg for barnet.', 'Søker har ikke omsorg for barnet.',
   to_date('2000-01-01', 'YYYY-MM-DD'), 'AVKORTING_AARSAK_TYPE');
