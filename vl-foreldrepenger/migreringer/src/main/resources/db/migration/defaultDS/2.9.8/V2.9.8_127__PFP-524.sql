insert into KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'RE-KLAG-M-INNTK', 'Klage/ankebehandling med endrede inntektsopplysninger', 'BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
values (seq_kodeliste.nextval, 'RE-KLAG-U-INNTK', 'Klage/ankebehandling uten endrede inntektsopplysninger', 'BEHANDLING_AARSAK', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'BEHANDLING_AARSAK', 'RE-KLAG-M-INNTK', 'NB', 'Klage/ankebehandling med endrede inntektsopplysninger');
INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'BEHANDLING_AARSAK', 'RE-KLAG-U-INNTK', 'NB', 'Klage/ankebehandling uten endrede inntektsopplysninger');
/*
DELETE FROM KODELISTE_NAVN_I18N WHERE KL_KODEVERK='BEHANDLING_AARSAK' AND KL_KODE='RE-KLAG';
DELETE FROM KODELISTE WHERE KODEVERK='BEHANDLING_AARSAK' AND KODE='RE-KLAG';

-- Migrere RE-KLAG til RE-KLAG-U-INNTK
UPDATE BEHANDLING_ARSAK set BEHANDLING_ARSAK_TYPE='RE-KLAG-U-INNTK' WHERE BEHANDLING_ARSAK_TYPE='RE-KLAG';
*/