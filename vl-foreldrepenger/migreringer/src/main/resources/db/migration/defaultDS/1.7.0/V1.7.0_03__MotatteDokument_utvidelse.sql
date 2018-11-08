ALTER TABLE MOTTATTE_DOKUMENT ADD (MOTTATT_DATO DATE);
DELETE FROM DOKUMENT_TYPE;
INSERT INTO DOKUMENT_TYPE (KODE, NAVN, BESKRIVELSE) VALUES ('I000003', 'Søknad om engangsstønad ved fødsel', 'Søknad om engangsstønad ved fødsel');
INSERT INTO DOKUMENT_TYPE (KODE, NAVN, BESKRIVELSE) VALUES ('I000004', 'Søknad om engangsstønad ved adopsjon', 'Søknad om engangsstønad ved adopsjon');
INSERT INTO DOKUMENT_TYPE (KODE, NAVN, BESKRIVELSE) VALUES ('I000041', 'Dokumentasjon av termin eller fødsel', 'Dokumentasjon av termindato (lev. kun av mor), fødsel eller dato for omsorgsovertakelse');
INSERT INTO DOKUMENT_TYPE (KODE, NAVN, BESKRIVELSE) VALUES ('I000042', 'Dokumentasjon av omsorgsovertakelse', 'Dokumentasjon av dato for overtakelse av omsorg');
INSERT INTO DOKUMENT_TYPE (KODE, NAVN, BESKRIVELSE) VALUES ('ANNET_DOKUMENT', 'Alle andre dokumenter', 'Alle andre dokumenter');