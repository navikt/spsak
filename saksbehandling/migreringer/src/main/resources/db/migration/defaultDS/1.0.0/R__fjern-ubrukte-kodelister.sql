DELETE FROM KODELISTE_NAVN_I18N WHERE KL_KODEVERK = 'FAMILIE_HENDELSE_TYPE';
DELETE FROM KODELISTE WHERE KODEVERK = 'FAMILIE_HENDELSE_TYPE';
DELETE FROM KODEVERK WHERE KODE  = 'FAMILIE_HENDELSE_TYPE';

DELETE FROM KODELISTE_NAVN_I18N WHERE KL_KODEVERK = 'RELASJONSROLLE_TYPE';
DELETE FROM KODELISTE WHERE KODEVERK = 'RELASJONSROLLE_TYPE';
DELETE FROM KODEVERK WHERE KODE  = 'RELASJONSROLLE_TYPE';

UPDATE KONFIG_VERDI SET KONFIG_VERDI = 'P4W' WHERE KONFIG_KODE = 'opptjeningsperiode.lengde';

DELETE FROM KODELISTE_NAVN_I18N WHERE KL_KODEVERK = 'OPPGAVE_AARSAK';
DELETE FROM KODELISTE WHERE KODEVERK = 'OPPGAVE_AARSAK';
DELETE FROM KODEVERK WHERE KODE  = 'OPPGAVE_AARSAK';
