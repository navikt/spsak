INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) VALUES
  ('foreldelsesfrist.foreldrenger.år', 'Foreldelsesfrist foreldrepenger', 'INGEN', 'INTEGER',
   ' Foreldelsesfrist i år (positivt heltall), før fagsak avsluttes');
INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
VALUES (SEQ_KONFIG_VERDI.nextval, 'foreldelsesfrist.foreldrenger.år', 'INGEN', '3', to_date('01.01.2016', 'dd.mm.yyyy'));
