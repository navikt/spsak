--trenger ikke denne statusen lenger, den er er stattet med 'FREMOVERFØRT'.
-- ved fremoverføring kan et steg spesifisere hva som er neste steg

UPDATE BEHANDLING_STEG_TILSTAND
SET BEHANDLING_STEG_STATUS = 'FREMOVERFØRT'
WHERE BEHANDLING_STEG_STATUS = 'FREMOVERFØRT_MED_VEDTAK';

DELETE from kodeliste
WHERE kode = 'FREMOVERFØRT_MED_VEDTAK' AND KODEVERK = 'BEHANDLING_STEG_STATUS';
