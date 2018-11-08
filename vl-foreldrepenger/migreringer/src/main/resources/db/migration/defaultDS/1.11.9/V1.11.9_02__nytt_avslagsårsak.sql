INSERT INTO KODELISTE (id, kode, navn, kodeverk, gyldig_fom) 
VALUES (seq_kodeliste.nextval, '1031', 'engangstønad er allerede utbetalt til mor', 'AVSLAGSARSAK', to_date('2000-01-01', 'yyyy-mm-dd'));

INSERT INTO vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) VALUES ('1031', 'FP_VK_1');
INSERT INTO vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) VALUES ('1031', 'FP_VK_4');
INSERT INTO vilkar_type_avslagsarsak_koble(avslagsarsak_kode, vilkar_type_kode) VALUES ('1031', 'FP_VK_5');