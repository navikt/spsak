INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) VALUES
  ('innsyn.klagefrist.uker', 'Innsyn klagefrist', 'INGEN', 'INTEGER',
   ' Klagefrist i uker (positivt heltall), sendes i svar på innsynskrav til brukeren');
INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
VALUES (SEQ_KONFIG_VERDI.nextval, 'innsyn.klagefrist.uker', 'INGEN', '3', to_date('01.01.2016', 'dd.mm.yyyy'));
