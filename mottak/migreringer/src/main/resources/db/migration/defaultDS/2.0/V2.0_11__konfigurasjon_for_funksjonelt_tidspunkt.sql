INSERT INTO KONFIG_VERDI_KODE (KODE, NAVN, KONFIG_GRUPPE, KONFIG_TYPE, BESKRIVELSE, OPPRETTET_TID) VALUES ('funksjonelt.tidsoffset.offset', 'Offset for funksjonell nåtid', 'INGEN', 'PERIOD', 'Offset antall dager for funksjonelt nå-tidspunkt', to_date('12.12.2017', 'dd.mm.yyyy'));
INSERT INTO KONFIG_VERDI (ID, KONFIG_KODE, KONFIG_GRUPPE, KONFIG_VERDI, GYLDIG_FOM) VALUES (SEQ_KONFIG_VERDI.nextval, 'funksjonelt.tidsoffset.offset', 'INGEN', 'P0D', to_date('12.12.2017', 'dd.mm.yyyy'));
