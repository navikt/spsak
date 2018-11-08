INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
VALUES ('norg2.kontakt.klageinstans.telefonnummer', 'Norg2 Nav Klageinstans kontakttelefon', 'INGEN', 'STRING', 'Norg2 Nav Klageinstans kontakttelefon');
INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
VALUES (SEQ_KONFIG_VERDI.nextval, 'norg2.kontakt.klageinstans.telefonnummer', 'INGEN', '21071730', to_date('05.12.2017', 'dd.mm.yyyy'));
