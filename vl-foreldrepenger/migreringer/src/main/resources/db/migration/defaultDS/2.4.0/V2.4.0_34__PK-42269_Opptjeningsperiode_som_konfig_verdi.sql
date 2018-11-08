INSERT INTO KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
VALUES ('opptjeningsperiode.lengde', 'Lengden på opptjeningsperioden.', 'INGEN', 'PERIOD', 'Lengden på opptjeningsperioden.');

INSERT INTO KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi, gyldig_fom)
VALUES (SEQ_KONFIG_VERDI.nextval, 'opptjeningsperiode.lengde', 'INGEN', 'P10M', to_date('01.01.2017', 'dd.mm.yyyy'));
