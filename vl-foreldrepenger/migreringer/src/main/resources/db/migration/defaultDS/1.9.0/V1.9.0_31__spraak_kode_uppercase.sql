-- nye språkkoder
INSERT INTO SPRAAK_KODE (kode, navn) VALUES ('NB', 'Norsk bokmål');
INSERT INTO SPRAAK_KODE (kode, navn) VALUES ('NN', 'Norsk nynorsk');
INSERT INTO SPRAAK_KODE (kode, navn) VALUES ('EN', 'Engelsk');

-- oppdatere avhengige tabeller
UPDATE DOKUMENT_FELLES set SPRAAK_KODE = 'NB' where SPRAAK_KODE = 'nb';
UPDATE DOKUMENT_FELLES set SPRAAK_KODE = 'NN' where SPRAAK_KODE = 'nn';
UPDATE DOKUMENT_FELLES set SPRAAK_KODE = 'EN' where SPRAAK_KODE = 'en';

UPDATE BRUKER set SPRAAK_KODE = 'NB' where SPRAAK_KODE = 'nb';
UPDATE BRUKER set SPRAAK_KODE = 'NN' where SPRAAK_KODE = 'nn';
UPDATE BRUKER set SPRAAK_KODE = 'EN' where SPRAAK_KODE = 'en';

-- fjern gamle språkkoder
delete SPRAAK_KODE where kode in ('nb', 'nn', 'en');

alter table BRUKER MODIFY SPRAAK_KODE default 'NB';



