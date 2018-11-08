ALTER TABLE YF_FORDELING ADD annenForelderErInformert CHAR(1) DEFAULT 'N' NOT NULL;
COMMENT ON COLUMN YF_FORDELING.annenForelderErInformert IS 'Om det er huket av for at den andre forelder er kjent med hvilke perioder det er søkt om';

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5013', 'Ikke samtykke mellom foreldrene', 'Ikke samtykke mellom foreldrene', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5014', 'Vurder samtidig uttak', 'Vurder samtidig uttak', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
