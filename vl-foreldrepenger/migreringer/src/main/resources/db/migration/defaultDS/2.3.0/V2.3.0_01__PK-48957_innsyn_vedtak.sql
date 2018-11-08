INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'VEDTAK_I_INNSYNBEHANDLING', 'vedtak i innsynbehandling', 'vedtak i innsynbehandling',
        'VEDTAK_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));


INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'INNSYN_AVVIST', 'Innsynskrav er avvist', 'Innsynskrav er avvist', 'BEHANDLING_RESULTAT_TYPE',
   to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'INNSYN_INNVILGET', 'Innsynskrav er innvilget', 'Innsynskrav er innvilget', 'BEHANDLING_RESULTAT_TYPE',
   to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom) VALUES
  (seq_kodeliste.nextval, 'INNSYN_DELVIS_INNVILGET', 'Innsynskrav er delvis innvilget', 'Innsynskrav er delvis innvilget', 'BEHANDLING_RESULTAT_TYPE',
   to_date('2000-01-01', 'YYYY-MM-DD'));
