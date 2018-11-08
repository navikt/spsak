-- Ny grunnbeløp sats for 2018

update sats set tom = to_date('2018-04-30', 'YYYY-MM-DD') where sats_type = 'GRUNNBELØP' and tom = to_date('2099-12-31', 'YYYY-MM-DD');

INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GRUNNBELØP', to_date('2018-05-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 96883);
INSERT INTO SATS (ID, SATS_TYPE, FOM, TOM, VERDI) VALUES (seq_sats.nextval, 'GSNITT', to_date('2018-01-01', 'YYYY-MM-DD'), to_date('2018-12-31', 'YYYY-MM-DD'), 95800);
