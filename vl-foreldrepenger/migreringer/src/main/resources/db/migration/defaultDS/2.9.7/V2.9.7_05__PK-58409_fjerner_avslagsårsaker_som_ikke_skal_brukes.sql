UPDATE UTTAK_RESULTAT_PERIODE set PERIODE_RESULTAT_AARSAK = '-' WHERE PERIODE_RESULTAT_AARSAK ='4001';
UPDATE UTTAK_RESULTAT_PERIODE set PERIODE_RESULTAT_AARSAK = '-' WHERE PERIODE_RESULTAT_AARSAK ='4010';
UPDATE UTTAK_RESULTAT_PERIODE set PERIODE_RESULTAT_AARSAK = '-' WHERE PERIODE_RESULTAT_AARSAK ='4011';
UPDATE UTTAK_RESULTAT_PERIODE set PERIODE_RESULTAT_AARSAK = '-' WHERE PERIODE_RESULTAT_AARSAK ='4014';
UPDATE UTTAK_RESULTAT_PERIODE set PERIODE_RESULTAT_AARSAK = '-' WHERE PERIODE_RESULTAT_AARSAK ='4015';
UPDATE UTTAK_RESULTAT_PERIODE set PERIODE_RESULTAT_AARSAK = '-' WHERE PERIODE_RESULTAT_AARSAK ='4017';

DELETE FROM KODELISTE WHERE KODEVERK = 'IKKE_OPPFYLT_AARSAK' AND KODE = '4001';
DELETE FROM KODELISTE WHERE KODEVERK = 'IKKE_OPPFYLT_AARSAK' AND KODE = '4010';
DELETE FROM KODELISTE WHERE KODEVERK = 'IKKE_OPPFYLT_AARSAK' AND KODE = '4011';
DELETE FROM KODELISTE WHERE KODEVERK = 'IKKE_OPPFYLT_AARSAK' AND KODE = '4014';
DELETE FROM KODELISTE WHERE KODEVERK = 'IKKE_OPPFYLT_AARSAK' AND KODE = '4015';
DELETE FROM KODELISTE WHERE KODEVERK = 'IKKE_OPPFYLT_AARSAK' AND KODE = '4017';
