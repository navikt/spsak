DELETE FROM KODELISTE_NAVN_I18N WHERE KL_KODEVERK = 'FAMILIE_HENDELSE_TYPE';
DELETE FROM KODELISTE WHERE KODEVERK = 'FAMILIE_HENDELSE_TYPE';
DELETE FROM KODEVERK WHERE KODE  = 'FAMILIE_HENDELSE_TYPE';

UPDATE KONFIG_VERDI SET KONFIG_VERDI = 'P4W' WHERE KONFIG_KODE = 'opptjeningsperiode.lengde';
