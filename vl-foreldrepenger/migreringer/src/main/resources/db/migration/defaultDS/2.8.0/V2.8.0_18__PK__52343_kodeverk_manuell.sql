INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5001', 'Stønadskonto tom for stønadsdager', 'Stønadskonto tom for stønadsdager', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

UPDATE KODELISTE SET BESKRIVELSE = 'Ugyldig stønadskonto' WHERE KODE = '5002' AND KODEVERK = 'MANUELL_BEHANDLING_AARSAK';
UPDATE KODELISTE SET BESKRIVELSE = 'Begrunnelse ikke gyldig' WHERE KODE = '5003' AND KODEVERK = 'MANUELL_BEHANDLING_AARSAK';

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5004', 'Aktivitetskravet må sjekkes manuelt', 'Aktivitetskravet må sjekkes manuelt', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

UPDATE KODELISTE SET BESKRIVELSE = 'Manglende søkt periode' WHERE KODE = '5005' AND KODEVERK = 'MANUELL_BEHANDLING_AARSAK';
UPDATE KODELISTE SET BESKRIVELSE = 'Avklar arbeid' WHERE KODE = '5006' AND KODEVERK = 'MANUELL_BEHANDLING_AARSAK';

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5007', 'Adopsjon ikke implementert', 'Adopsjon ikke implementert', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5008', 'Foreldrepenger ikke implementert', 'Foreldrepenger ikke implementert', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5009', 'Søker har ikke omsorg for barnet', 'Søker har ikke omsorg for barnet', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5010', 'Uttak ikke gyldig pga søknadsfrist', 'Uttak ikke gyldig pga søknadsfrist', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, '5011', 'Ikke gyldig grunn for utsettelse', 'Ikke gyldig grunn for utsettelse', 'MANUELL_BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

