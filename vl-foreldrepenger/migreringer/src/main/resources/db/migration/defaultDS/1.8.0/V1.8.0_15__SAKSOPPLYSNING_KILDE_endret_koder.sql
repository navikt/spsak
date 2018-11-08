-- PKLIBELLE-114 harmonisere saksopplysning-kildekoder med fagsystemkoder

UPDATE saksopplysning_kilde SET kode = 'FS03' WHERE kode = 'TPS';
UPDATE saksopplysning_kilde SET kode = 'AS36' WHERE kode = 'JOARK';
UPDATE saksopplysning_kilde SET kode = 'IT01' WHERE kode = 'INFOTRY';

INSERT INTO saksopplysning_kilde (kode, navn) VALUES ('AO01', 'ARENA');
