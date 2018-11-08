-- Nytt kodeverk SKJERMLENKE_TYPE
INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier) VALUES ('SKJERMLENKE_TYPE', 'Kodeverk for skjermlenketyper', 'Kodeverk for skjermlenketyper','VL');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'KONTROLL_AV_SAKSOPPLYSNINGER', 'Kontroll av saksopplysninger', 'Kontroll av saksopplysninger', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'BEREGNING_ENGANGSSTOENAD', 'Beregning engangsstønad', 'Beregning engangsstønad', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'BEREGNING_FORELDREPENGER', 'Beregning foreldrepenger', 'Beregning foreldrepenger', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'VEDTAK', 'Vedtak', 'Vedtak', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'ADOPSJON', 'Adopsjon', 'Adopsjon', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'SOEKNADSFRIST', 'Søknadsfrist', 'Søknadsfrist', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FOEDSEL', 'Fødsel', 'Fødsel', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'MEDLEMSKAP', 'Medlemskap', 'Medlemskap', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OPPLYSNINGSPLIKT', 'Opplysningsplikt', 'Opplysningsplikt', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OMSORG', 'Omsorg', 'Omsorg', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FORELDREANSVAR', 'Foreldreansvar', 'Foreldreansvar', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'BEHANDLE_KLAGE', 'Behandle klage', 'Behandle klage', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FAKTA_FOR_OPPTJENING', 'Fakta for opptjening', 'Fakta for opptjening', to_date('2000-01-01', 'YYYY-MM-DD'), 'SKJERMLENKE_TYPE');
